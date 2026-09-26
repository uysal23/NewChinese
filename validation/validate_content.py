import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def fail(message: str) -> None:
    raise SystemExit(f"VALIDATION FAILED: {message}")

for i in range(1, 21):
    matches = list(ROOT.glob(f"LOCKED_STEP_{i:02d}_*.md"))
    if len(matches) != 1:
        fail(f"Expected one locked manifest for step {i}; found {len(matches)}")

manifest = json.loads((ROOT / "content/content_manifest.json").read_text(encoding="utf-8"))
levels = manifest.get("levels", [])
if len(levels) != 6 or sum(int(x["sceneCount"]) for x in levels) != 300:
    fail("Content manifest must declare 6 levels and exactly 300 target scenes")

required = [
    "scene.json", "dialogue.json", "vocabulary.json", "sentence_exercises.json",
    "word_exam.json", "sentence_exam.json", "visual_manifest.json", "audio_manifest.json"
]

scene_dirs = sorted(
    d for level_dir in ROOT.glob("content/hsk[1-6]")
    for d in level_dir.glob("sc[0-9][0-9][0-9]")
    if d.is_dir()
)
if not scene_dirs:
    fail("At least one scene must exist")

seen_line_ids = set()
seen_scene_ids = set()

for scene_dir in scene_dirs:
    level_num = int(scene_dir.parent.name.replace("hsk", ""))
    scene_num = int(scene_dir.name.replace("sc", ""))
    expected_scene_id = f"HSK{level_num}_SC{scene_num:03d}"

    for name in required:
        if not (scene_dir / name).exists():
            fail(f"{expected_scene_id} missing {name}")

    scene = json.loads((scene_dir / "scene.json").read_text(encoding="utf-8"))
    dialogue = json.loads((scene_dir / "dialogue.json").read_text(encoding="utf-8"))
    word_exam = json.loads((scene_dir / "word_exam.json").read_text(encoding="utf-8"))
    sentence_exam = json.loads((scene_dir / "sentence_exam.json").read_text(encoding="utf-8"))
    visual = json.loads((scene_dir / "visual_manifest.json").read_text(encoding="utf-8"))
    media_status_path = scene_dir / "media_status.json"
    media_status = json.loads(media_status_path.read_text(encoding="utf-8")) if media_status_path.exists() else None

    scene_id = scene.get("sceneId")
    if scene_id != expected_scene_id:
        fail(f"{scene_dir}: sceneId must be {expected_scene_id}; found {scene_id}")
    if scene_id in seen_scene_ids:
        fail(f"Duplicate sceneId: {scene_id}")
    seen_scene_ids.add(scene_id)

    character_ids = scene.get("characterIds", [])
    if len(character_ids) != 2:
        fail(f"{scene_id} must have exactly two active characters")

    if word_exam.get("passingScore") != 90:
        fail(f"{scene_id} word exam threshold must be 90")
    if sentence_exam.get("passingScore") != 85:
        fail(f"{scene_id} sentence exam threshold must be 85")

    for line in dialogue.get("lines", []):
        lid = line.get("lineId")
        if not lid or lid in seen_line_ids:
            fail(f"Dialogue line IDs must be unique; problem at {scene_id}: {lid}")
        seen_line_ids.add(lid)

        speaker = line.get("speakerId")
        if speaker not in character_ids:
            fail(f"{lid} uses speaker {speaker} outside scene characterIds")

        character_path = ROOT / "characters" / speaker / "character.json"
        if not character_path.exists():
            fail(f"{lid} references missing character {speaker}")
        character = json.loads(character_path.read_text(encoding="utf-8"))
        expected_voice = character.get("voiceId")
        if line.get("voiceId") != expected_voice:
            fail(f"Voice binding mismatch at {lid}: expected {expected_voice}")

        for key in ("textZh", "pinyin", "translationTr", "audioFile"):
            if not line.get(key):
                fail(f"{lid} missing {key}")

    bubble = visual.get("activeSpeakerBubble", {})
    if bubble.get("outline") != "dashed" or bubble.get("text") is not False:
        fail(f"{scene_id} active speaker bubble must be dashed and textless")

    if media_status is not None:
        for category in ("visual", "audio"):
            block = media_status.get(category, {})
            status = block.get("status")
            required_assets = block.get("required", [])
            if status not in ("pending", "complete"):
                fail(f"{scene_id} {category} media status must be pending or complete")
            if not required_assets:
                fail(f"{scene_id} {category} media required list cannot be empty")
            if status == "complete":
                for rel in required_assets:
                    asset_path = scene_dir / rel
                    if not asset_path.exists():
                        fail(f"{scene_id} {category} marked complete but missing physical asset: {rel}")

placement = json.loads((ROOT / "content/placement/placement_test.json").read_text(encoding="utf-8"))
questions = placement.get("questions", [])
if len(questions) != 30:
    fail("Placement test must contain exactly 30 questions")
for level in [f"HSK{i}" for i in range(1, 7)]:
    count = sum(1 for q in questions if q.get("level") == level)
    if count != 5:
        fail(f"Placement test must contain exactly 5 questions for {level}; found {count}")

print("Validation PASS")
print("Locked manifests: 20/20")
print("Target scenes declared: 300")
print(f"Present scene packages validated: {len(scene_dirs)}")
pending_media = 0
for scene_dir in scene_dirs:
    media_path = scene_dir / "media_status.json"
    if media_path.exists():
        media = json.loads(media_path.read_text(encoding="utf-8"))
        pending_media += sum(1 for k in ("visual", "audio") if media.get(k, {}).get("status") == "pending")
print(f"Pending media categories: {pending_media}")
print("Placement test: 30 questions / 5 per HSK level PASS")
