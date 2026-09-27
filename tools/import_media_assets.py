from __future__ import annotations

import json
import re
import shutil
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INCOMING = ROOT / "incoming_assets"
PACKAGE_RE = re.compile(
    r"^(HSK([1-6])_SC(\d{3}))_(visual|audio)_assets\.zip$",
    re.IGNORECASE,
)
BATCH_RE = re.compile(
    r"^HSK([1-6])_SC(\d{3})_SC(\d{3})_(visual|audio)_assets_batch\.zip$",
    re.IGNORECASE,
)

ALLOWED_SUFFIXES = {
    "visual": {".webp", ".png", ".jpg", ".jpeg"},
    "audio": {".m4a", ".aac", ".mp3", ".wav"},
}


def fail(message: str) -> None:
    raise RuntimeError(message)


def safe_members(archive: zipfile.ZipFile):
    for info in archive.infolist():
        if info.is_dir():
            continue
        path = Path(info.filename)
        if path.is_absolute() or ".." in path.parts:
            fail(f"Unsafe ZIP member: {info.filename}")
        yield info


def load_media_status(scene_dir: Path) -> tuple[Path, dict]:
    status_path = scene_dir / "media_status.json"
    if not status_path.exists():
        fail(f"Missing media_status.json: {status_path.relative_to(ROOT)}")
    return status_path, json.loads(status_path.read_text(encoding="utf-8"))


def required_basenames(media_status: dict, category: str) -> set[str]:
    block = media_status.get(category) or {}
    required = block.get("required") or []
    if not required:
        fail(f"No required assets declared for {category}")
    return {Path(item).name for item in required}


def verify_and_mark(scene_dir: Path, status_path: Path, media_status: dict, category: str) -> None:
    required = media_status[category]["required"]
    missing = [rel for rel in required if not (scene_dir / rel).is_file()]
    media_status[category]["status"] = "pending" if missing else "complete"
    media_status[category]["missing"] = missing
    status_path.write_text(
        json.dumps(media_status, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def import_package(zip_path: Path) -> None:
    match = PACKAGE_RE.match(zip_path.name)
    if not match:
        fail(
            f"Unsupported package name: {zip_path.name}. "
            "Expected HSKx_SCyyy_visual_assets.zip or HSKx_SCyyy_audio_assets.zip"
        )

    scene_id, level, scene_number, category = match.groups()
    category = category.lower()
    scene_dir = ROOT / "content" / f"hsk{level}" / f"sc{scene_number}"
    if not scene_dir.is_dir():
        fail(f"Scene directory does not exist for {scene_id}: {scene_dir.relative_to(ROOT)}")

    status_path, media_status = load_media_status(scene_dir)
    expected = required_basenames(media_status, category)
    assets_dir = scene_dir / "assets"
    assets_dir.mkdir(parents=True, exist_ok=True)

    extracted: set[str] = set()
    with zipfile.ZipFile(zip_path) as archive:
        for info in safe_members(archive):
            filename = Path(info.filename).name
            suffix = Path(filename).suffix.lower()
            if suffix not in ALLOWED_SUFFIXES[category]:
                fail(f"Unsupported {category} file type in {zip_path.name}: {filename}")
            if filename not in expected:
                fail(
                    f"Unexpected file in {zip_path.name}: {filename}. "
                    f"Expected one of: {', '.join(sorted(expected))}"
                )
            destination = assets_dir / filename
            with archive.open(info) as source, destination.open("wb") as target:
                shutil.copyfileobj(source, target)
            extracted.add(filename)

    if not extracted:
        fail(f"No usable files found in {zip_path.name}")

    verify_and_mark(scene_dir, status_path, media_status, category)
    zip_path.unlink()

    missing = media_status[category].get("missing", [])
    print(f"Imported {zip_path.name} -> {scene_dir.relative_to(ROOT) / 'assets'}")
    if missing:
        print(f"{category} remains pending; missing: {', '.join(missing)}")
    else:
        print(f"{category} status: complete")


def expand_batch_package(batch_path: Path) -> None:
    match = BATCH_RE.match(batch_path.name)
    if not match:
        return
    level, start_s, end_s, category = match.groups()
    start, end = int(start_s), int(end_s)
    if start > end:
        fail(f"Invalid batch range: {batch_path.name}")
    expected = {
        f"HSK{level}_SC{n:03d}_{category.lower()}_assets.zip"
        for n in range(start, end + 1)
    }
    found: set[str] = set()
    with zipfile.ZipFile(batch_path) as archive:
        for info in safe_members(archive):
            filename = Path(info.filename).name
            if filename == "PACKAGE_MANIFEST.json":
                continue
            if filename not in expected:
                fail(
                    f"Unexpected nested package in {batch_path.name}: {filename}. "
                    f"Expected scene packages for SC{start:03d}-SC{end:03d}."
                )
            destination = INCOMING / filename
            with archive.open(info) as source, destination.open("wb") as target:
                shutil.copyfileobj(source, target)
            found.add(filename)
    missing = sorted(expected - found)
    if missing:
        fail(f"Missing nested packages in {batch_path.name}: {', '.join(missing)}")
    batch_path.unlink()
    print(f"Expanded batch {batch_path.name}: {len(found)} scene packages.")


def main() -> int:
    INCOMING.mkdir(exist_ok=True)
    initial = sorted(INCOMING.glob("*.zip"))
    if not initial:
        print("No media ZIP packages found in incoming_assets.")
        return 0

    for package in initial:
        if BATCH_RE.match(package.name):
            expand_batch_package(package)

    packages = sorted(
        path for path in INCOMING.glob("*.zip")
        if PACKAGE_RE.match(path.name)
    )
    unsupported = sorted(
        path.name for path in INCOMING.glob("*.zip")
        if not PACKAGE_RE.match(path.name)
    )
    if unsupported:
        fail(f"Unsupported media ZIP package(s): {', '.join(unsupported)}")

    for package in packages:
        import_package(package)
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"MEDIA IMPORT FAILED: {exc}", file=sys.stderr)
        raise
