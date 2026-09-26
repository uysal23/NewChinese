from __future__ import annotations

import base64
import re
import zipfile
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PARTS_DIR = ROOT / "incoming_asset_parts"
OUT_DIR = ROOT / "incoming_assets"

PART_RE = re.compile(
    r"^(HSK[1-6]_SC\d{3}_(?:visual|audio)_assets\.zip)\.b64\.part(\d{2,})$"
)
VISUAL_RE = re.compile(
    r"^(HSK[1-6]_SC\d{3})__(hsk[1-6]_sc\d{3}_.+\.webp)\.b64$"
)


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

        decoded: list[tuple[str, bytes]] = []
        for asset_name, source in sorted(assets):
            payload = decode_b64_text(source)
            if not payload.startswith(b"RIFF") or b"WEBP" not in payload[:16]:
                raise RuntimeError(f"Decoded visual is not WebP: {source.name}")
            decoded.append((asset_name, payload))

        if destination.exists():
            destination.unlink()

        with zipfile.ZipFile(destination, "w", compression=zipfile.ZIP_DEFLATED) as archive:
            for asset_name, payload in decoded:
                archive.writestr(asset_name, payload)

        print(
            f"Built {destination.relative_to(ROOT)} "
            f"from {len(decoded)} staged WebP assets."
        )
        count += 1
    return count


def main() -> int:
    if not PARTS_DIR.exists():
        print("No incoming_asset_parts directory; nothing to reconstruct.")
        return 0

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    packages = reconstruct_package_parts()
    visuals = reconstruct_visual_packages()
    print(f"Reconstructed packages: {packages}; visual packages: {visuals}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
