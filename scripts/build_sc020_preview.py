from PIL import Image
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "content/hsk3/sc020/assets"
W, H = 540, 960

def load_rgba(name):
    im = Image.open(ASSETS / name).convert("RGBA")
    return im.resize((W, H), Image.Resampling.LANCZOS)

bg = Image.open(ASSETS / "hsk3_sc020_bg.webp").convert("RGB").resize((W, H), Image.Resampling.LANCZOS)
char_a = load_rgba("hsk3_sc020_char_zhang_wei.webp")
char_b = load_rgba("hsk3_sc020_char_liu_mei.webp")
fg = load_rgba("hsk3_sc020_fg.webp")

for name, im in [
    ("hsk3_sc020_char_zhang_wei.webp", char_a),
    ("hsk3_sc020_char_liu_mei.webp", char_b),
    ("hsk3_sc020_fg.webp", fg),
]:
    if "A" not in im.getbands():
        raise SystemExit(f"missing alpha: {name}")

preview = bg.convert("RGBA")
preview.alpha_composite(char_a)
preview.alpha_composite(char_b)
preview.alpha_composite(fg)

out = ASSETS / "hsk3_sc020_preview.webp"
preview.convert("RGB").save(out, "WEBP", quality=90, method=6)

check = Image.open(out)
check.load()
if check.size != (W, H):
    raise SystemExit(f"bad preview size: {check.size}")

print("SC020 preview generated and QA passed")
