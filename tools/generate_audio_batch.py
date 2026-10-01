from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GENERATOR = ROOT / "tools" / "generate_audio_assets.py"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--batch-index", type=int, required=True)
    parser.add_argument("--batch-count", type=int, required=True)
    parser.add_argument("--output-dir", required=True)
    parser.add_argument("--levels", default="3,4,5,6")
    args = parser.parse_args()

    if args.batch_count < 1 or not 0 <= args.batch_index < args.batch_count:
        raise RuntimeError("Invalid batch index/count")

    levels = {int(x.strip()) for x in args.levels.split(",") if x.strip()}
    output_dir = (ROOT / args.output_dir).resolve()
    if ROOT not in output_dir.parents:
        raise RuntimeError("Output directory must remain inside repository")
    output_dir.mkdir(parents=True, exist_ok=True)

    queues: list[Path] = []
    for level in sorted(levels):
        queues.extend(sorted((ROOT / "content" / f"hsk{level}").glob("sc*/audio_production_queue.json")))

    selected = [q for i, q in enumerate(queues) if i % args.batch_count == args.batch_index]
    print(
        f"Audio shard {args.batch_index}/{args.batch_count}: "
        f"{len(selected)} scenes from {len(queues)} total queues"
    )

    if not selected:
        return 0

    for queue_path in selected:
        queue = json.loads(queue_path.read_text(encoding="utf-8"))
        package_name = str(queue["packageName"])
        output_path = output_dir / package_name
        print(f"Generate {queue['sceneId']} -> {output_path.relative_to(ROOT)}")
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

    print(f"Completed shard {args.batch_index}: {len(selected)} scene packages")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
