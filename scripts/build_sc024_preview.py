from PIL import Image
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "content/hsk3/sc024/assets"
W, H = 540, 960

def fit_char(path: Path, target_h: int, x: int, bottom: int = 925):
    im = Image.open(path).convert("RGBA")
    bbox = im.getchannel("A").getbbox()
    if bbox:
        l,t,r,b = bbox
        pad = 6
        im = im.crop((max(0,l-pad),max(0,t-pad),min(im.width,r+pad),min(im.height,b+pad)))
    scale = target_h / im.height
    im = im.resize((max(1,int(im.width*scale)),target_h), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA",(W,H),(0,0,0,0))
    canvas.alpha_composite(im,(x,bottom-target_h))
    return canvas

bg = Image.open(ASSETS/"hsk3_sc024_bg.webp").convert("RGB").resize((W,H),Image.Resampling.LANCZOS)
zw = fit_char(ASSETS/"hsk3_sc024_char_zhang_wei.webp",700,-55)
ln = fit_char(ASSETS/"hsk3_sc024_char_li_na.webp",700,255)
fg = Image.open(ASSETS/"hsk3_sc024_fg.webp").convert("RGBA").resize((W,H),Image.Resampling.LANCZOS)

preview = bg.convert("RGBA")
preview.alpha_composite(zw)
preview.alpha_composite(ln)
preview.alpha_composite(fg)
out = ASSETS/"hsk3_sc024_preview.webp"
preview.convert("RGB").save(out,"WEBP",quality=88,method=6)

for name in ["hsk3_sc024_bg.webp","hsk3_sc024_char_zhang_wei.webp","hsk3_sc024_char_li_na.webp","hsk3_sc024_fg.webp","hsk3_sc024_preview.webp"]:
    p=ASSETS/name
    im=Image.open(p); im.load()

if Image.open(out).size != (W,H):
    raise SystemExit("bad preview size")
print("SC024 preview generated")
