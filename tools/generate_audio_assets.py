from __future__ import annotations

import argparse
import asyncio
import json
import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

import edge_tts

ROOT = Path(__file__).resolve().parents[1]

VOICE_MAP = {
    "VOICE_LI_NA_001": "zh-CN-XiaoxiaoNeural",
    "VOICE_ZHANG_WEI_001": "zh-CN-YunxiNeural",
    "VOICE_WANG_MING_001": "zh-CN-YunjianNeural",
}

RATE_BY_TYPE = {
    "dialogue": "+0%",
    "vocabulary": "-8%",
    "sentence": "-3%",
}

PITCH_BY_VOICE = {
    "VOICE_LI_NA_001": "+0Hz",
    "VOICE_ZHANG_WEI_001": "+0Hz",
    "VOICE_WANG_MING_001": "+0Hz",
}


def fail(message: str) -> None:
    raise RuntimeError(message)


def run_checked(*args: str) -> None:
    print("+", " ".join(args))
    subprocess.run(args, check=True)


def probe_duration(path: Path) -> float:
    completed = subprocess.run(
        [
            "ffprobe",
            "-v",
            "error",
            "-show_entries",
            "format=duration",
            "-of",
            "default=noprint_wrappers=1:nokey=1",
            str(path),
        ],
        check=True,
        capture_output=True,
        text=True,
    )
    return float(completed.stdout.strip())


async def synthesize_mp3(text: str, voice: str, rate: str, pitch: str, destination: Path) -> None:
    # Edge TTS occasionally returns no audio for an otherwise valid request.
    # Retry the exact same synthesis request with bounded backoff before failing
    # the scene. This keeps GitHub-only production resilient to transient service
    # errors without changing voice identity, rate, pitch, or text.
    attempts = 5
    last_error: Exception | None = None

    for attempt in range(1, attempts + 1):
        if destination.exists():
            destination.unlink()

        try:
            communicate = edge_tts.Communicate(
                text=text,
                voice=voice,
                rate=rate,
                pitch=pitch,
            )
            await communicate.save(str(destination))

            if destination.is_file() and destination.stat().st_size > 0:
                if attempt > 1:
                    print(f"TTS recovered on attempt {attempt}/{attempts}.")
                return

            raise RuntimeError("TTS returned an empty audio file")
        except Exception as exc:
            last_error = exc
            if attempt >= attempts:
                break

            delay_seconds = min(2 * attempt, 8)
            print(
                f"TTS attempt {attempt}/{attempts} failed: {exc}. "
                f"Retrying in {delay_seconds}s..."
            )
            await asyncio.sleep(delay_seconds)

    raise RuntimeError(
        f"TTS failed after {attempts} attempts for voice={voice}: {last_error}"
    )


async def generate_item(item: dict, temp_dir: Path, output_dir: Path) -> Path:
    filename = item["file"]
    if not filename.lower().endswith(".m4a"):
        fail(f"Queue output must be .m4a: {filename}")

    voice_id = item["voiceId"]
    voice = VOICE_MAP.get(voice_id)
    if not voice:
        fail(f"No Edge TTS voice mapping for {voice_id}")

    text = str(item["textZh"]).strip()
    if not text:
        fail(f"Empty Mandarin text for {filename}")

    item_type = item.get("type", "dialogue")
    rate = RATE_BY_TYPE.get(item_type, "+0%")
    pitch = PITCH_BY_VOICE.get(voice_id, "+0Hz")

    source_mp3 = temp_dir / (Path(filename).stem + ".mp3")
    output_m4a = output_dir / filename

    print(f"Generating {filename} | {voice_id} -> {voice} | {text}")
    await synthesize_mp3(text, voice, rate, pitch, source_mp3)

    run_checked(
        "ffmpeg",
        "-hide_banner",
        "-loglevel",
        "error",
        "-y",
        "-i",
        str(source_mp3),
        "-af",
        "loudnorm=I=-18:TP=-2:LRA=7",
        "-c:a",
        "aac",
        "-b:a",
        "128k",
        "-ar",
        "44100",
        "-ac",
        "1",
        str(output_m4a),
    )

    if not output_m4a.is_file() or output_m4a.stat().st_size < 1024:
        fail(f"Invalid generated audio file: {filename}")

    duration = probe_duration(output_m4a)
    minimum_duration = 0.25 if item_type == "vocabulary" else 0.45
    if duration < minimum_duration:
        fail(
            f"Generated audio is too short ({duration:.3f}s < {minimum_duration:.2f}s): "
            f"{filename}"
        )

    return output_m4a


async def generate(queue_path: Path, output_zip: Path) -> None:
    queue = json.loads(queue_path.read_text(encoding="utf-8"))
    items = queue.get("items") or []
    if not items:
        fail("Audio production queue is empty")

    declared_package = queue.get("packageName")
    if declared_package and output_zip.name != declared_package:
        fail(
            f"Output ZIP name must match queue packageName: "
            f"{output_zip.name} != {declared_package}"
        )

    expected_files = [item["file"] for item in items]
    if len(expected_files) != len(set(expected_files)):
        fail("Duplicate output filenames in audio production queue")

    output_zip.parent.mkdir(parents=True, exist_ok=True)

    with tempfile.TemporaryDirectory(prefix="newchinese-audio-") as tmp:
        temp_dir = Path(tmp) / "sources"
        output_dir = Path(tmp) / "m4a"
        temp_dir.mkdir(parents=True)
        output_dir.mkdir(parents=True)

        generated: list[Path] = []
        for item in sorted(items, key=lambda x: int(x.get("order", 0))):
            generated.append(await generate_item(item, temp_dir, output_dir))

        generated_names = {p.name for p in generated}
        expected_names = set(expected_files)
        if generated_names != expected_names:
            fail(
                f"Generated filenames do not match queue. "
                f"missing={sorted(expected_names - generated_names)}, "
                f"unexpected={sorted(generated_names - expected_names)}"
            )

        if output_zip.exists():
            output_zip.unlink()

        with zipfile.ZipFile(output_zip, "w", compression=zipfile.ZIP_DEFLATED) as archive:
            for path in sorted(generated, key=lambda p: p.name):
                archive.write(path, arcname=path.name)

    if not output_zip.is_file() or output_zip.stat().st_size < 1024:
        fail(f"Audio ZIP was not created correctly: {output_zip}")

    with zipfile.ZipFile(output_zip) as archive:
        zipped = {Path(name).name for name in archive.namelist() if not name.endswith("/")}
    expected = set(expected_files)
    if zipped != expected:
        fail(
            f"ZIP contents mismatch. missing={sorted(expected - zipped)}, "
            f"unexpected={sorted(zipped - expected)}"
        )

    print(f"Created {output_zip.relative_to(ROOT)} with {len(expected)} audio files.")


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate locked Mandarin audio assets with Edge TTS.")
    parser.add_argument(
        "--queue",
        default="content/hsk1/sc001/audio_production_queue.json",
        help="Path to the audio production queue relative to repository root.",
    )
    parser.add_argument(
        "--output",
        default="incoming_assets/HSK1_SC001_audio_assets.zip",
        help="ZIP output path relative to repository root.",
    )
    args = parser.parse_args()

    queue_path = (ROOT / args.queue).resolve()
    output_zip = (ROOT / args.output).resolve()

    if not queue_path.is_file():
        fail(f"Queue file not found: {queue_path}")
    if ROOT not in output_zip.parents:
        fail("Output ZIP must remain inside the repository")

    asyncio.run(generate(queue_path, output_zip))
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"AUDIO GENERATION FAILED: {exc}", file=sys.stderr)
        raise
