from __future__ import annotations

import base64
import json
import re
import zipfile
from collections import defaultdict
from io import BytesIO
from pathlib import Path

from PIL import Image, ImageEnhance

ROOT = Path(__file__).resolve().parents[1]
PARTS_DIR = ROOT / "incoming_asset_parts"
OUT_DIR = ROOT / "incoming_assets"

PART_RE = re.compile(
    r"^(HSK[1-6]_SC\d{3}_(?:visual|audio)_assets\.zip)\.b64\.part(\d{2,})$"
)
VISUAL_RE = re.compile(
    r"^(HSK[1-6]_SC\d{3})__(hsk[1-6]_sc\d{3}_.+\.webp)\.b64$"
)

# Step-6 daily-routine scenes may intentionally return to a previously established
# location after intervening scenes. These overrides preserve location continuity
# instead of blindly borrowing the immediately previous scene background.
RECOVERY_SOURCE_OVERRIDES = {
    "HSK1_SC016": ("013", 1.00),
    "HSK1_SC017": ("016", 0.985),
    "HSK1_SC018": ("017", 1.015),
    "HSK1_SC020": ("018", 0.58),
}


def decode_b64_text(path: Path) -> bytes:
    encoded = path.read_text(encoding="utf-8").strip()
    try:
        return base64.b64decode(encoded, validate=True)
    except Exception as exc:
        raise RuntimeError(f"Invalid base64 in {path.name}: {exc}") from exc


def reconstruct_package_parts() -> int:
    groups: dict[str, list[tuple[int, Path]]] = defaultdict(list)
    for path in sorted(PARTS_DIR.iterdir()):
        if not path.is_file():
            continue
        match = PART_RE.match(path.name)
        if match:
            groups[match.group(1)].append((int(match.group(2)), path))

    count = 0
    for package_name, parts in sorted(groups.items()):
        parts.sort(key=lambda item: item[0])
        expected = list(range(len(parts)))
        actual = [n for n, _ in parts]
        if actual != expected:
            raise RuntimeError(
                f"Non-contiguous parts for {package_name}: "
                f"expected={expected}, actual={actual}"
            )

        encoded = "".join(
            part.read_text(encoding="utf-8").strip()
            for _, part in parts
        )
        try:
            payload = base64.b64decode(encoded, validate=True)
        except Exception as exc:
            raise RuntimeError(f"Invalid base64 for {package_name}: {exc}") from exc

        if not payload.startswith(b"PK\x03\x04"):
            raise RuntimeError(f"Reconstructed file is not a ZIP: {package_name}")

        destination = OUT_DIR / package_name
        destination.write_bytes(payload)
        print(f"Reconstructed {destination.relative_to(ROOT)} from {len(parts)} parts.")
        count += 1
    return count


def reconstruct_visual_packages() -> int:
    groups: dict[str, list[tuple[str, Path]]] = defaultdict(list)
    for path in sorted(PARTS_DIR.iterdir()):
        if not path.is_file():
            continue
        match = VISUAL_RE.match(path.name)
        if match:
            groups[match.group(1)].append((match.group(2), path))

    count = 0
    for scene_id, assets in sorted(groups.items()):
        package_name = f"{scene_id}_visual_assets.zip"
        destination = OUT_DIR / package_name

        decoded: dict[str, bytes] = {}
        scene_error: Exception | None = None
        for asset_name, source in sorted(assets):
            try:
                payload = decode_b64_text(source)
                if not payload.startswith(b"RIFF") or b"WEBP" not in payload[:16]:
                    raise RuntimeError(f"Decoded visual is not WebP: {source.name}")
                decoded[asset_name] = payload
            except Exception as exc:
                scene_error = exc
                break

        if scene_error is not None:
            # For consecutive station scenes, recover a damaged staged background
            # from the immediately previous completed scene while preserving the
            # locked visual identity. Apply a small framing/brightness variation
            # so the next scene is not an identical still.
            scene_match = re.match(r"^HSK1_SC(\d{3})$", scene_id)
            recovered = False
            if scene_match:
                number_int = int(scene_match.group(1))
                if number_int > 1:
                    default_source = f"{number_int - 1:03d}"
                    source_number, brightness = RECOVERY_SOURCE_OVERRIDES.get(
                        scene_id, (default_source, 1.035)
                    )
                    previous_bg = (
                        ROOT
                        / "content"
                        / "hsk1"
                        / f"sc{source_number}"
                        / "assets"
                        / f"hsk1_sc{source_number}_bg.webp"
                    )
                    if previous_bg.is_file():
                        base = Image.open(previous_bg).convert("RGB")
                        w, h = base.size
                        crop_x = max(1, int(w * 0.025))
                        crop_y = max(1, int(h * 0.015))
                        shifted = base.crop((crop_x, crop_y, w, h))
                        shifted = shifted.resize((w, h), Image.Resampling.LANCZOS)
                        shifted = ImageEnhance.Brightness(shifted).enhance(brightness)
                        out = BytesIO()
                        shifted.save(out, "WEBP", quality=82, method=6)
                        recovered_name = f"hsk1_sc{scene_match.group(1)}_bg.webp"
                        decoded = {recovered_name: out.getvalue()}
                        recovered = True
                        print(
                            f"Recovered {scene_id} background from "
                            f"{previous_bg.relative_to(ROOT)} after staging error: {scene_error}"
                        )
            if not recovered:
                if destination.exists():
                    destination.unlink()
                print(f"Skipping {scene_id}: {scene_error}")
                continue

        # Consecutive HSK1 station scenes intentionally reuse the phone-approved
        # SC001 character identity layers and safe foreground. This prevents
        # face/outfit drift while each scene keeps its own background/preview.
        scene_match = re.match(r"^HSK1_SC(\d{3})$", scene_id)
        if scene_match and scene_id != "HSK1_SC001":
            number = scene_match.group(1)
            if int(number) >= 11:
                li_reference = ROOT / "content/hsk1/sc011/assets/hsk1_sc011_char_li_na.webp"
                zw_reference = ROOT / "content/hsk1/sc011/assets/hsk1_sc011_char_zhang_wei.webp"
                fg_reference = ROOT / "content/hsk1/sc011/assets/hsk1_sc011_fg.webp"
            else:
                li_reference = ROOT / "content/hsk1/sc001/assets/hsk1_sc001_char_li_na.webp"
                zw_reference = ROOT / "content/hsk1/sc001/assets/hsk1_sc001_char_zhang_wei.webp"
                fg_reference = ROOT / "content/hsk1/sc001/assets/hsk1_sc001_fg.webp"

            reference_assets = {
                f"hsk1_sc{number}_char_li_na.webp": li_reference,
                f"hsk1_sc{number}_char_zhang_wei.webp": zw_reference,
                f"hsk1_sc{number}_fg.webp": fg_reference,
            }
            for target_name, reference_path in reference_assets.items():
                if target_name not in decoded:
                    if not reference_path.is_file():
                        raise RuntimeError(f"Missing locked reference asset: {reference_path}")
                    decoded[target_name] = reference_path.read_bytes()

            background_name = f"hsk1_sc{number}_bg.webp"
            preview_name = f"hsk1_sc{number}_preview.webp"
            li_name = f"hsk1_sc{number}_char_li_na.webp"
            zw_name = f"hsk1_sc{number}_char_zhang_wei.webp"
            fg_name = f"hsk1_sc{number}_fg.webp"

            if preview_name not in decoded and background_name in decoded:
                background = Image.open(BytesIO(decoded[background_name])).convert("RGBA")
                canvas = background.copy()

                if fg_name in decoded:
                    fg = Image.open(BytesIO(decoded[fg_name])).convert("RGBA")
                    fg.thumbnail(canvas.size, Image.Resampling.LANCZOS)
                    canvas.alpha_composite(fg, (0, canvas.height - fg.height))

                def place_character(asset_name: str, x_fraction: float) -> None:
                    if asset_name not in decoded:
                        return
                    char = Image.open(BytesIO(decoded[asset_name])).convert("RGBA")
                    target_h = int(canvas.height * 0.76)
                    scale = target_h / max(char.height, 1)
                    char = char.resize(
                        (max(1, int(char.width * scale)), target_h),
                        Image.Resampling.LANCZOS,
                    )
                    x = int(canvas.width * x_fraction - char.width / 2)
                    y = canvas.height - char.height
                    canvas.alpha_composite(char, (x, y))

                place_character(li_name, 0.34)
                place_character(zw_name, 0.67)

                output = BytesIO()
                canvas.convert("RGB").save(output, "WEBP", quality=78, method=6)
                decoded[preview_name] = output.getvalue()

        # HSK2 layered scenes reuse the latest approved canonical character layer
        # for each declared character, while BG/FG remain scene-specific. This
        # preserves face/outfit continuity and lets staged BG/FG packages build
        # their merged preview deterministically.
        hsk2_match = re.match(r"^HSK2_SC(\\d{3})$", scene_id)
        if hsk2_match:
            number = hsk2_match.group(1)
            scene_number = int(number)
            scene_dir = ROOT / "content" / "hsk2" / f"sc{number}"
            manifest_path = scene_dir / "visual_manifest.json"
            if not manifest_path.is_file():
                raise RuntimeError(f"Missing visual manifest: {manifest_path}")
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))

            def character_slug(character_id: str) -> str:
                slug = character_id.lower()
                if slug.startswith("char_"):
                    slug = slug[5:]
                slug = re.sub(r"_\\d{3}$", "", slug)
                return slug

            character_names: list[str] = []
            for spec in manifest.get("characters", []):
                slug = character_slug(spec["characterId"])
                target_name = f"hsk2_sc{number}_char_{slug}.webp"
                if target_name not in decoded:
                    reference_path: Path | None = None
                    for previous in range(scene_number - 1, 0, -1):
                        candidate = (
                            ROOT / "content" / "hsk2" / f"sc{previous:03d}" /
                            "assets" / f"hsk2_sc{previous:03d}_char_{slug}.webp"
                        )
                        if candidate.is_file():
                            reference_path = candidate
                            break
                    if reference_path is None:
                        raise RuntimeError(
                            f"No approved canonical HSK2 character layer found for {spec['characterId']}"
                        )
                    decoded[target_name] = reference_path.read_bytes()
                    print(
                        f"Reused canonical character for {scene_id}: "
                        f"{reference_path.relative_to(ROOT)} -> {target_name}"
                    )
                character_names.append(target_name)

            background_name = f"hsk2_sc{number}_bg.webp"
            foreground_name = f"hsk2_sc{number}_fg.webp"
            preview_name = f"hsk2_sc{number}_preview.webp"

            if preview_name not in decoded and background_name in decoded:
                background = Image.open(BytesIO(decoded[background_name])).convert("RGBA")
                canvas = background.copy()

                # Runtime draws foreground before character layers so opaque/partial
                # foreground art cannot mask character bodies.
                if foreground_name in decoded:
                    foreground = Image.open(BytesIO(decoded[foreground_name])).convert("RGBA")
                    if foreground.size != canvas.size:
                        foreground = foreground.resize(canvas.size, Image.Resampling.LANCZOS)
                    canvas.alpha_composite(foreground, (0, 0))

                positions = [0.34, 0.67]
                for index, asset_name in enumerate(character_names[:2]):
                    if asset_name not in decoded:
                        continue
                    char = Image.open(BytesIO(decoded[asset_name])).convert("RGBA")
                    target_h = int(canvas.height * 0.76)
                    scale = target_h / max(char.height, 1)
                    char = char.resize(
                        (max(1, int(char.width * scale)), target_h),
                        Image.Resampling.LANCZOS,
                    )
                    x_fraction = positions[index] if index < len(positions) else 0.5
                    x = int(canvas.width * x_fraction - char.width / 2)
                    y = canvas.height - char.height
                    canvas.alpha_composite(char, (x, y))

                output = BytesIO()
                canvas.convert("RGB").save(output, "WEBP", quality=78, method=6)
                decoded[preview_name] = output.getvalue()

        if destination.exists():
            destination.unlink()

        with zipfile.ZipFile(destination, "w", compression=zipfile.ZIP_DEFLATED) as archive:
            for asset_name, payload in sorted(decoded.items()):
                archive.writestr(asset_name, payload)

        print(
            f"Built {destination.relative_to(ROOT)} "
            f"from {len(decoded)} WebP assets."
        )
        count += 1
    return count



def reconstruct_hsk2_sc048_from_refs() -> int:
    marker = PARTS_DIR / "HSK2_SC048_FROM_REFS"
    if not marker.is_file():
        return 0

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    source_bg = ROOT / "content/hsk2/sc043/assets/hsk2_sc043_bg.webp"
    source_fg = ROOT / "content/hsk2/sc043/assets/hsk2_sc043_fg.webp"
    source_zw = ROOT / "content/hsk2/sc047/assets/hsk2_sc047_char_zhang_wei.webp"
    source_cy = ROOT / "content/hsk2/sc047/assets/hsk2_sc047_char_chen_yu.webp"
    sources = [source_bg, source_fg, source_zw, source_cy]
    missing = [p for p in sources if not p.is_file()]
    if missing:
        raise RuntimeError(
            "Missing SC048 reference asset(s): "
            + ", ".join(str(p.relative_to(ROOT)) for p in missing)
        )

    background = Image.open(source_bg).convert("RGB")
    width, height = background.size

    # Reframe the approved lakeside source into a generic Hangzhou city-park view.
    # Cropping the far-right side removes the West Lake landmark/pagoda while
    # retaining lake, trees, bridge/walkway and family-friendly green-space cues.
    crop_right = max(1, int(width * 0.76))
    background = background.crop((0, 0, crop_right, height))
    background = background.resize((width, height), Image.Resampling.LANCZOS)
    background = ImageEnhance.Color(background).enhance(0.95)
    background = ImageEnhance.Brightness(background).enhance(1.02)

    foreground = Image.open(source_fg).convert("RGBA")
    if foreground.size != (width, height):
        foreground = foreground.resize((width, height), Image.Resampling.LANCZOS)

    assets: dict[str, bytes] = {}

    out = BytesIO()
    background.save(out, "WEBP", quality=78, method=6)
    assets["hsk2_sc048_bg.webp"] = out.getvalue()

    assets["hsk2_sc048_char_zhang_wei.webp"] = source_zw.read_bytes()
    assets["hsk2_sc048_char_chen_yu.webp"] = source_cy.read_bytes()

    out = BytesIO()
    foreground.save(out, "WEBP", quality=82, method=6)
    assets["hsk2_sc048_fg.webp"] = out.getvalue()

    canvas = background.convert("RGBA")
    canvas.alpha_composite(foreground, (0, 0))

    def place_character(payload: bytes, x_fraction: float) -> None:
        char = Image.open(BytesIO(payload)).convert("RGBA")
        target_h = int(height * 0.76)
        scale = target_h / max(char.height, 1)
        char = char.resize(
            (max(1, int(char.width * scale)), target_h),
            Image.Resampling.LANCZOS,
        )
        x = int(width * x_fraction - char.width / 2)
        y = height - char.height
        canvas.alpha_composite(char, (x, y))

    place_character(assets["hsk2_sc048_char_zhang_wei.webp"], 0.34)
    place_character(assets["hsk2_sc048_char_chen_yu.webp"], 0.67)

    out = BytesIO()
    canvas.convert("RGB").save(out, "WEBP", quality=78, method=6)
    assets["hsk2_sc048_preview.webp"] = out.getvalue()

    destination = OUT_DIR / "HSK2_SC048_visual_assets.zip"
    if destination.exists():
        destination.unlink()
    with zipfile.ZipFile(destination, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for asset_name, payload in sorted(assets.items()):
            archive.writestr(asset_name, payload)

    print(
        f"Built {destination.relative_to(ROOT)} from approved SC043/SC047 references "
        "for HSK2_SC048."
    )
    return 1

def main() -> int:
    if not PARTS_DIR.exists():
        print("No incoming_asset_parts directory; nothing to reconstruct.")
        return 0

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    special_sc048 = reconstruct_hsk2_sc048_from_refs()
    packages = reconstruct_package_parts()
    visuals = reconstruct_visual_packages()
    print(f"Reconstructed packages: {packages}; visual packages: {visuals}; special SC048: {special_sc048}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
