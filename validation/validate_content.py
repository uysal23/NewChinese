import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def fail(message: str) -> None:
    raise SystemExit(f"VALIDATION FAILED: {message}")

for i in range(1, 21):
    matches = list(ROOT.glob(f"LOCKED_STEP_{i:02d}_*.md"))
    if len(matches) != 1:
        fail(f"Expected one locked manifest for step {i}; found {len(matches)}")

manifest_path = ROOT / "content/content_manifest.json"
manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
levels = manifest.get("levels", [])
if len(levels) != 6 or sum(int(x["sceneCount"]) for x in levels) != 300:
    fail("Content manifest must declare 6 levels and exactly 300 target scenes")

scene_dir = ROOT / "content/hsk1/sc001"
required = [
    "scene.json","dialogue.json","vocabulary.json","sentence_exercises.json",
    "word_exam.json","sentence_exam.json","visual_manifest.json","audio_manifest.json"
]
for name in required:
    if not (scene_dir / name).exists():
        fail(f"HSK1_SC001 missing {name}")

scene = json.loads((scene_dir/"scene.json").read_text(encoding="utf-8"))
dialogue = json.loads((scene_dir/"dialogue.json").read_text(encoding="utf-8"))
word_exam = json.loads((scene_dir/"word_exam.json").read_text(encoding="utf-8"))
sentence_exam = json.loads((scene_dir/"sentence_exam.json").read_text(encoding="utf-8"))

if scene.get("sceneId") != "HSK1_SC001":
    fail("Reference scene ID mismatch")
if len(scene.get("characterIds", [])) != 2:
    fail("Reference scene must have exactly two active characters")
if word_exam.get("passingScore") != 90:
    fail("Word exam threshold must be 90")
if sentence_exam.get("passingScore") != 85:
    fail("Sentence exam threshold must be 85")

voice_map = {
    "CHAR_ZHANG_WEI_001":"VOICE_ZHANG_WEI_001",
    "CHAR_LI_NA_001":"VOICE_LI_NA_001",
}
seen_line_ids=set()
for line in dialogue.get("lines", []):
    lid=line.get("lineId")
    if not lid or lid in seen_line_ids:
        fail("Dialogue line IDs must be present and unique")
    seen_line_ids.add(lid)
    speaker=line.get("speakerId")
    if voice_map.get(speaker) != line.get("voiceId"):
        fail(f"Voice binding mismatch at {lid}")
    for key in ("textZh","pinyin","translationTr","audioFile"):
        if not line.get(key):
            fail(f"{lid} missing {key}")

visual=json.loads((scene_dir/"visual_manifest.json").read_text(encoding="utf-8"))
bubble=visual.get("activeSpeakerBubble",{})
if bubble.get("outline")!="dashed" or bubble.get("text") is not False:
    fail("Active speaker bubble must be dashed and textless")

print("Validation PASS")
print("Locked manifests: 20/20")
print("Target scenes declared: 300")
print("Reference scene HSK1_SC001: schema PASS")

placement = json.loads((ROOT / "content/placement/placement_test.json").read_text(encoding="utf-8"))
questions = placement.get("questions", [])
if len(questions) != 30:
    fail("Placement test must contain exactly 30 questions")
for level in [f"HSK{i}" for i in range(1, 7)]:
    count = sum(1 for q in questions if q.get("level") == level)
    if count != 5:
        fail(f"Placement test must contain exactly 5 questions for {level}; found {count}")
