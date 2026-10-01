from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BATCH_RE = re.compile(r"^HSK([1-6])_SC(\d{3})_SC(\d{3})\.json$")


def dump(path: Path, obj: object) -> None:
    path.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def scene_status(scene_dir: Path) -> tuple[bool, bool]:
    media_path = scene_dir / "media_status.json"
    if not media_path.is_file():
        return False, False
    media = json.loads(media_path.read_text(encoding="utf-8"))
    for category in ("visual", "audio"):
        block = media.get(category) or {}
        required = block.get("required") or []
        missing = [rel for rel in required if not (scene_dir / rel).is_file()]
        block["missing"] = missing
        block["status"] = "pending" if missing else "complete"
        media[category] = block
    dump(media_path, media)

    audio_manifest_path = scene_dir / "audio_manifest.json"
    if audio_manifest_path.is_file():
        manifest = json.loads(audio_manifest_path.read_text(encoding="utf-8"))
        manifest["status"] = (
            "audio-assets-complete"
            if media["audio"]["status"] == "complete"
            else "audio-assets-pending"
        )
        dump(audio_manifest_path, manifest)

    return media["visual"]["status"] == "complete", media["audio"]["status"] == "complete"


def combined_status(visual_complete: bool, audio_complete: bool) -> str:
    if visual_complete and audio_complete:
        return "complete"
    if audio_complete:
        return "audio_complete_visual_pending"
    if visual_complete:
        return "visual_complete_audio_pending"
    return "content_ready_visual_audio_pending"


def update_batches(scene_states: dict[str, tuple[bool, bool]]) -> None:
    batch_dir = ROOT / "production_batches"
    for path in sorted(batch_dir.glob("HSK*_SC*_SC*.json")):
        if not BATCH_RE.match(path.name):
            continue
        data = json.loads(path.read_text(encoding="utf-8"))
        scenes = data.get("scenes") or []
        visual_count = 0
        audio_count = 0
        complete_count = 0
        for scene in scenes:
            scene_id = scene.get("sceneId")
            visual_complete, audio_complete = scene_states.get(scene_id, (False, False))
            scene["status"] = combined_status(visual_complete, audio_complete)
            visual_count += int(visual_complete)
            audio_count += int(audio_complete)
            complete_count += int(visual_complete and audio_complete)

        progress = data.setdefault("progress", {})
        total = len(scenes)
        progress["contentReady"] = total
        progress["totalScenes"] = total
        progress["visualComplete"] = visual_count
        progress["audioComplete"] = audio_count
        progress["acceptedScenes"] = complete_count
        if "completeScenes" in progress:
            progress["completeScenes"] = complete_count
        if "previewComplete" in progress:
            progress["previewComplete"] = visual_count
        if "previewImported" in progress:
            progress["previewImported"] = visual_count
        if "previewTotal" in progress:
            progress["previewTotal"] = total

        statuses = {scene["status"] for scene in scenes}
        if statuses == {"complete"}:
            data["status"] = "complete"
        elif statuses == {"audio_complete_visual_pending"}:
            data["status"] = "audio_complete_visual_pending"
        elif statuses == {"visual_complete_audio_pending"}:
            data["status"] = "visual_complete_audio_pending"
        else:
            data["status"] = "mixed_progress"

        dump(path, data)


def main() -> int:
    scene_states: dict[str, tuple[bool, bool]] = {}
    for level in range(1, 7):
        level_dir = ROOT / "content" / f"hsk{level}"
        if not level_dir.is_dir():
            continue
        for scene_dir in sorted(level_dir.glob("sc[0-9][0-9][0-9]")):
            scene_id = f"HSK{level}_SC{int(scene_dir.name[2:]):03d}"
            scene_states[scene_id] = scene_status(scene_dir)

    update_batches(scene_states)

    audio_complete = sum(1 for v in scene_states.values() if v[1])
    visual_complete = sum(1 for v in scene_states.values() if v[0])
    print(f"Scenes audited: {len(scene_states)}")
    print(f"Audio complete: {audio_complete}/{len(scene_states)}")
    print(f"Visual complete: {visual_complete}/{len(scene_states)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
