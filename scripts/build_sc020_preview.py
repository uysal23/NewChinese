from PIL import Image
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "content/hsk3/sc020/assets"
W, H = 540, 960

def fit_char(path: Path, target_h: int, x: int, bottom: int = 930):
    im = Image.open(path).convert("RGBA")
    bbox = im.getchannel("A").getbbox()
    if bbox:
        l, t, r, b = bbox
        pad = 8
        l = max(0, l - pad)
        t = max(0, t - pad)
        r = min(im.width, r + pad)
        b = min(im.height, b + pad)
        im = im.crop((l, t, r, b))
    scale = target_h / im.height
    im = im.resize((max(1, int(im.width * scale)), target_h), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    canvas.alpha_composite(im, (x, bottom - target_h))
    return canvas

bg = Image.open(ASSETS / "hsk3_sc020_bg.webp").convert("RGB").resize((W, H), Image.Resampling.LANCZOS)
char_a = fit_char(ASSETS / "hsk3_sc020_char_zhang_wei.webp", 710, -45)
char_b = fit_char(ASSETS / "hsk3_sc020_char_liu_mei.webp", 700, 255)
fg = Image.open(ASSETS / "hsk3_sc020_fg.webp").convert("RGBA").resize((W, H), Image.Resampling.LANCZOS)

preview = bg.convert("RGBA")
preview.alpha_composite(char_a)
preview.alpha_composite(char_b)
preview.alpha_composite(fg)

out = ASSETS / "hsk3_sc020_preview.webp"
preview.convert("RGB").save(out, "WEBP", quality=88, method=6)

for name in [
    "hsk3_sc020_bg.webp",
    "hsk3_sc020_char_zhang_wei.webp",
    "hsk3_sc020_char_liu_mei.webp",
    "hsk3_sc020_fg.webp",
    "hsk3_sc020_preview.webp",
]:
    p = ASSETS / name
    im = Image.open(p)
    im.load()
    if name.startswith("hsk3_sc020_char_") or name == "hsk3_sc020_fg.webp":
        if "A" not in im.getbands():
            raise SystemExit(f"missing alpha: {name}")
    if name == "hsk3_sc020_preview.webp" and im.size != (W, H):
        raise SystemExit(f"bad preview size: {im.size}")

print("SC020 preview generated and QA passed")
