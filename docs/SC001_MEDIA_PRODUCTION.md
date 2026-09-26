# HSK1 SC001 — Media Production Contract

This document converts the locked visual/audio rules into the concrete production package for HSK1_SC001.

## Characters
- CHAR_LI_NA_001 → VOICE_LI_NA_001
- CHAR_ZHANG_WEI_001 → VOICE_ZHANG_WEI_001

Character identity, outfit presets, face, age and voice identity must follow the root locked manifests and character bibles.

## Location
- LOC_TRAIN_STATION_001
- Hangzhou railway station
- daytime
- two-shot composition
- subtitle-safe bottom zone
- Study safe area top-left
- Pinyin safe area top-right

## Active speaker indicator
The active speaker has a small speech bubble near the mouth/face anchor:
- white
- lightly opaque
- dashed outline
- no text
- only one visible at a time
- driven by speakerId, never baked permanently into the scene image

## Visual assets
Required:
- assets/hsk1_sc001_bg.webp
- assets/hsk1_sc001_char_li_na.webp
- assets/hsk1_sc001_char_zhang_wei.webp
- assets/hsk1_sc001_fg.webp
- assets/hsk1_sc001_preview.webp

The speech bubble itself should be rendered by the UI so it always follows the active speaker.

## Dialogue audio
Every dialogue line must use the locked character voice:
- line 001 Li Na
- line 002 Zhang Wei
- line 003 Li Na
- line 004 Zhang Wei
- line 005 Li Na
- line 006 Zhang Wei

Required files:
- assets/hsk1_sc001_line_001.m4a
- assets/hsk1_sc001_line_002.m4a
- assets/hsk1_sc001_line_003.m4a
- assets/hsk1_sc001_line_004.m4a
- assets/hsk1_sc001_line_005.m4a
- assets/hsk1_sc001_line_006.m4a

## Study audio
Vocabulary and correct-sentence audio must also be natural Mandarin, not phone TTS.

## Completion rule
SC001 media is not COMPLETE merely because manifests exist. All required files must physically exist, load in-app, match their Character/Voice/Location binding, and pass visual/audio quality validation.
