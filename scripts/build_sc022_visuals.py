from PIL import Image, ImageOps, ImageDraw, ImageFilter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "content/hsk1/sc030/assets"
OUT = ROOT / "content/hsk3/sc022/assets"
OUT.mkdir(parents=True, exist_ok=True)
W, H = 540, 960

def load_rgba(path):
    im = Image.open(path).convert("RGBA")
    im.load()
    return im

def save_char(src_name, out_name):
    im = load_rgba(SRC / src_name)
    if "A" not in im.getbands() or im.getchannel("A").getbbox() is None:
        raise SystemExit(f"invalid transparent character source: {src_name}")
    im = ImageOps.mirror(im)
    im.save(OUT / out_name, "WEBP", lossless=True, method=6)

bg = Image.open(SRC / "hsk1_sc030_bg.webp").convert("RGB")
bg.load()
bg = ImageOps.fit(bg, (W, H), method=Image.Resampling.LANCZOS)
bg.save(OUT / "hsk3_sc022_bg.webp", "WEBP", quality=90, method=6)

save_char("hsk1_sc030_char_zhang_wei.webp", "hsk3_sc022_char_zhang_wei.webp")
save_char("hsk1_sc030_char_li_na.webp", "hsk3_sc022_char_li_na.webp")

fg = Image.new("RGBA", (W, H), (0, 0, 0, 0))
soft = Image.new("RGBA", (W, H), (0, 0, 0, 0))
d = ImageDraw.Draw(soft)
d.ellipse((-140, 805, 210, 1080), fill=(42, 58, 39, 28))
d.ellipse((350, 815, 690, 1085), fill=(42, 58, 39, 24))
soft = soft.filter(ImageFilter.GaussianBlur(32))
fg.alpha_composite(soft)
fg.save(OUT / "hsk3_sc022_fg.webp", "WEBP", lossless=True, method=6)

def placed_char(path, target_h, x, bottom=925):
    im = load_rgba(path)
    bbox = im.getchannel("A").getbbox()
    if not bbox:
        raise SystemExit(f"empty alpha: {path.name}")
    l,t,r,b = bbox
    im = im.crop((l,t,r,b))
    scale = target_h / im.height
    im = im.resize((max(1,int(im.width*scale)), target_h), Image.Resampling.LANCZOS)
    layer = Image.new("RGBA", (W,H), (0,0,0,0))
    layer.alpha_composite(im, (x, bottom-target_h))
    return layer

preview = bg.convert("RGBA")
preview.alpha_composite(placed_char(OUT/"hsk3_sc022_char_zhang_wei.webp", 700, -45))
preview.alpha_composite(placed_char(OUT/"hsk3_sc022_char_li_na.webp", 700, 300))
preview.alpha_composite(fg)
preview.convert("RGB").save(OUT / "hsk3_sc022_preview.webp", "WEBP", quality=90, method=6)

required = [
 "hsk3_sc022_bg.webp",
 "hsk3_sc022_char_zhang_wei.webp",
 "hsk3_sc022_char_li_na.webp",
 "hsk3_sc022_fg.webp",
 "hsk3_sc022_preview.webp",
]
for name in required:
    p = OUT / name
    im = Image.open(p)
    im.load()
    if name in {"hsk3_sc022_bg.webp","hsk3_sc022_preview.webp"} and im.size != (W,H):
        raise SystemExit(f"bad size {name}: {im.size}")
    if name.startswith("hsk3_sc022_char_") or name == "hsk3_sc022_fg.webp":
        if "A" not in im.getbands():
            raise SystemExit(f"missing alpha: {name}")
print("HSK3 SC022 visual generation QA passed")
