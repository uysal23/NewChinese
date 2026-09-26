from __future__ import annotations

import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INCOMING = ROOT / "incoming_assets"
GENERATOR = ROOT / "tools" / "generate_audio_assets.py"


def main() -> int:
    INCOMING.mkdir(exist_ok=True)
    queues = sorted(ROOT.glob("content/hsk*/sc*/audio_production_queue.json"))
    generated = 0

    for queue_path in queues:
        queue = json.loads(queue_path.read_text(encoding="utf-8"))
        scene_dir = queue_path.parent
        status_path = scene_dir / "media_status.json"
        if not status_path.is_file():
            print(f"Skip {queue_path.relative_to(ROOT)}: media_status.json missing")
            continue

        status = json.loads(status_path.read_text(encoding="utf-8"))
        audio = status.get("audio") or {}
        if audio.get("status") == "complete" and not (audio.get("missing") or []):
            print(f"Skip {queue.get('sceneId', scene_dir.name)}: audio already complete")
            continue

        package_name = str(queue.get("packageName") or "").strip()
        if not package_name:
            raise RuntimeError(f"Missing packageName in {queue_path.relative_to(ROOT)}")

        output_path = INCOMING / package_name
        print(f"Generate pending audio: {queue.get('sceneId')} -> {package_name}")
        subprocess.run(
            [
                sys.executable,
                str(GENERATOR),
                "--queue",
                str(queue_path.relative_to(ROOT)),
                "--output",
                str(output_path.relative_to(ROOT)),
            ],
            cwd=ROOT,
            check=True,
        )
        generated += 1

    print(f"Generated pending audio packages: {generated}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
