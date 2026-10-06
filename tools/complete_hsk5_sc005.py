from __future__ import annotations

import json
import subprocess
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SCENE_DIR = ROOT / "content" / "hsk5" / "sc005"
ASSET_DIR = SCENE_DIR / "assets"

BG = ASSET_DIR / "hsk5_sc005_bg.webp"
ZHANG = ASSET_DIR / "hsk5_sc005_char_zhang_wei.webp"
WANG = ASSET_DIR / "hsk5_sc005_char_wang_ming.webp"
FG = ASSET_DIR / "hsk5_sc005_fg.webp"
PREVIEW = ASSET_DIR / "hsk5_sc005_preview.webp"

for path in (BG, ZHANG, WANG, FG):
    if not path.is_file():
        raise SystemExit(f"SC005 missing staged canonical asset: {path.relative_to(ROOT)}")

background = Image.open(BG).convert("RGBA")
canvas = background.copy()

foreground = Image.open(FG).convert("RGBA")
if foreground.size != canvas.size:
    foreground = foreground.resize(canvas.size, Image.Resampling.LANCZOS)
canvas.alpha_composite(foreground, (0, 0))

def place_character(path: Path, x_fraction: float) -> None:
    char = Image.open(path).convert("RGBA")
    target_h = int(canvas.height * 0.76)
    scale = target_h / max(char.height, 1)
    char = char.resize(
        (max(1, int(char.width * scale)), target_h),
        Image.Resampling.LANCZOS,
    )
    x = int(canvas.width * x_fraction - char.width / 2)
    y = canvas.height - char.height
    canvas.alpha_composite(char, (x, y))

# Locked canonical composition: Zhang Wei face_right on left, Wang Ming face_left on right.
place_character(ZHANG, 0.34)
place_character(WANG, 0.67)

canvas.convert("RGB").save(PREVIEW, "WEBP", quality=78, method=6)

with Image.open(PREVIEW) as check:
    width, height = check.size
if width <= 0 or height <= 0:
    raise SystemExit("SC005 preview has invalid dimensions")
ratio = width / height
if abs(ratio - (9 / 16)) > 0.02:
    raise SystemExit(f"SC005 preview is not 9:16: {width}x{height}, ratio={ratio:.5f}")

manifest_path = SCENE_DIR / "visual_manifest.json"
manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
manifest["status"] = "visual-assets-complete"
manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

media_path = SCENE_DIR / "media_status.json"
media = json.loads(media_path.read_text(encoding="utf-8"))
media["visual"]["status"] = "complete"
media["visual"]["missing"] = []
media["rule"] = "Audio and visuals may be complete only after every declared physical asset exists and validation passes."
media_path.write_text(json.dumps(media, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

grounding_path = SCENE_DIR / "visual_grounding.json"
grounding = json.loads(grounding_path.read_text(encoding="utf-8"))
grounding.setdefault("compositionMethod", {})["status"] = "approved"
grounding["compositionMethod"]["method"] = "canonical-layer-composite"
grounding["compositionMethod"]["note"] = (
    "Electronics Store BG/FG HSK3_SC042 accepted kaynağından; Zhang Wei/Wang Ming "
    "character layers HSK4_SC041 accepted kaynağından kullanıldı. Textless 9:16 "
    "PREVIEW staged dört canonical runtime katmanından repo deterministic PIL "
    "compositor mantığıyla fiziksel olarak türetildi."
)
grounding.setdefault("layerPlan", {})["status"] = "approved"
grounding["layerPlan"]["preview"] = "content/hsk5/sc005/assets/hsk5_sc005_preview.webp"
grounding_path.write_text(json.dumps(grounding, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

batch_path = ROOT / "production_batches" / "HSK5_SC001_SC010.json"
batch = json.loads(batch_path.read_text(encoding="utf-8"))
batch["status"] = "in_progress"
batch["progress"]["visualComplete"] = 5
batch["progress"]["audioComplete"] = 10
batch["progress"]["acceptedScenes"] = 5
for scene in batch["scenes"]:
    if scene["sceneId"] == "HSK5_SC005":
        scene["status"] = "complete"
        break
else:
    raise SystemExit("HSK5_SC005 not found in first HSK5 batch")

drop_prefixes = (
    "HSK5_SC005 grounding/layer planning approved",
    "HSK5_SC001-SC010 first batch remains 4/10",
    "HSK5_SC001-SC010 first batch progress: 4/10",
)
notes = [
    note for note in batch.get("notes", [])
    if not any(note.startswith(prefix) for prefix in drop_prefixes)
]
notes.append(
    "HSK5_SC005 visual + audio complete; canonical Electronics Store composite "
    "HSK3_SC042 BG/FG + HSK4_SC041 Zhang Wei/Wang Ming layers üzerinden "
    "deterministic textless 9:16 PREVIEW ile tamamlandı; "
    "HSK5_SC004 → HSK5_SC005 continuity approved."
)
notes.append(
    "HSK5_SC001-SC010 first batch progress: 5/10 visual complete, "
    "10/10 audio complete, 5/10 accepted."
)
batch["notes"] = notes
batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

master_path = ROOT / "MASTER_EXECUTION_STEPS.md"
master = master_path.read_text(encoding="utf-8")
replacements = [
    (
        "- HSK5_SC005: **4/5 canonical Electronics Store layers staged; PREVIEW pending**; "
        "HSK3_SC042 BG/FG + HSK4_SC041 Zhang Wei/Wang Ming layers kullanıldı; "
        "HSK5_SC004 → HSK5_SC005 continuity approved. Scene completion gate intentionally not passed.",
        "- HSK5_SC005: **5/5 canonical Electronics Store composite visual complete**; "
        "HSK3_SC042 BG/FG + HSK4_SC041 Zhang Wei/Wang Ming layers kullanıldı; "
        "textless 9:16 PREVIEW deterministic olarak türetildi; "
        "HSK5_SC004 → HSK5_SC005 continuity approved.",
    ),
    (
        "- HSK5_SC001–SC010 first batch: **4/10 visual complete, 10/10 audio complete**.",
        "- HSK5_SC001–SC010 first batch: **5/10 visual complete, 10/10 audio complete, 5/10 accepted**.",
    ),
    (
        "- **Anlık iş kalemi: HSK5_SC005 için staged BG + CHAR_A + CHAR_B + FG katmanlarından "
        "doğru textless 9:16 PREVIEW üret, fiziksel 5/5 validation yap ve yalnız sonra "
        "scene/batch acceptance'ı ilerlet. Actions tüketmemek için [skip ci] kullan.**",
        "- **Anlık iş kalemi: HSK5_SC006 için manifest + dialogue + continuity kontrolünü yapıp "
        "BG → CHAR_A → CHAR_B → FG → PREVIEW → repo → validation zincirini yürüt. "
        "Actions tüketmemek için [skip ci] kullan.**",
    ),
]
for old, new in replacements:
    if old not in master:
        raise SystemExit(f"MASTER expected text not found: {old[:80]}")
    master = master.replace(old, new, 1)
master_path.write_text(master, encoding="utf-8")

visual_required = [Path(item).name for item in media["visual"]["required"]]
audio_required = [Path(item).name for item in media["audio"]["required"]]
missing_visual = [name for name in visual_required if not (ASSET_DIR / name).is_file()]
missing_audio = [name for name in audio_required if not (ASSET_DIR / name).is_file()]
if missing_visual:
    raise SystemExit(f"SC005 missing visual assets: {missing_visual}")
if missing_audio:
    raise SystemExit(f"SC005 missing audio assets: {missing_audio}")
if len(visual_required) != 5:
    raise SystemExit(f"SC005 visual required count must be 5; got {len(visual_required)}")

print(f"SC005 PREVIEW: {width}x{height} ratio={ratio:.5f}")
print("SC005 VISUAL: 5/5 PASS")
print(f"SC005 AUDIO: {len(audio_required)}/{len(audio_required)} PASS")
print("SC005 BATCH: 5/10 visual, 10/10 audio, 5/10 accepted")

subprocess.run(["python", "validation/validate_content.py"], cwd=ROOT, check=True)
subprocess.run(["git", "diff", "--check"], cwd=ROOT, check=True)
print("SC005 COMPLETION VALIDATION PASS")
