# LOCKS — Source of Truth

All twenty planning steps are LOCKED. Read the relevant root manifest before changing implementation.

| Step | Manifest | Status |
|---|---|---|
| 01 | LOCKED_STEP_01_PRODUCT.md | LOCKED |
| 02 | LOCKED_STEP_02_UI_UX.md | LOCKED |
| 03 | LOCKED_STEP_03_ARCHITECTURE.md | LOCKED |
| 04 | LOCKED_STEP_04_REPO_STRUCTURE.md | LOCKED |
| 05 | LOCKED_STEP_05_DATA_SCHEMAS.md | LOCKED |
| 06 | LOCKED_STEP_06_CHARACTER_BIBLE.md | LOCKED |
| 07 | LOCKED_STEP_07_LOCATION_BIBLE.md | LOCKED |
| 08 | LOCKED_STEP_08_STORY_PLAN.md | LOCKED |
| 09 | LOCKED_STEP_09_HSK1_PLAN.md | LOCKED |
| 10 | LOCKED_STEP_10_HSK2_PLAN.md | LOCKED |
| 11 | LOCKED_STEP_11_HSK3_PLAN.md | LOCKED |
| 12 | LOCKED_STEP_12_HSK4_PLAN.md | LOCKED |
| 13 | LOCKED_STEP_13_HSK5_PLAN.md | LOCKED |
| 14 | LOCKED_STEP_14_HSK6_PLAN.md | LOCKED |
| 15 | LOCKED_STEP_15_DIALOGUE_SYSTEM.md | LOCKED |
| 16 | LOCKED_STEP_16_STUDY_SYSTEM.md | LOCKED |
| 17 | LOCKED_STEP_17_VISUAL_SYSTEM.md | LOCKED |
| 18 | LOCKED_STEP_18_AUDIO_SYSTEM.md | LOCKED |
| 19 | LOCKED_STEP_19_INTEGRATION.md | LOCKED |
| 20 | LOCKED_STEP_20_VALIDATION_BUILD.md | LOCKED |

## Non-negotiable global rules
- Simplified Chinese only for Chinese learning content.
- 6 levels × 50 scenes = 300 scenes.
- Two active speakers per scene.
- Character identity and voice identity are persistent.
- Main character audio is natural Mandarin, not phone TTS.
- Family-friendly content: no alcohol, bars/nightclubs, mini-skirts, sexual/explicit content or erotic presentation.
- Word exam pass: 90%.
- Sentence exam pass: 85%.
- Shadowing default/minimum: 6 sentences; UI choices 6 / 10 / 15 / Entire dialogue.
- Root manifests are the source of truth for future work.

## LOCKED GOLDEN DEMO V1 — SC001 reference implementation
The phone-tested HSK1_SC001 experience is the locked reference for all subsequent scenes.
- Preserve the current dialogue UI layout and safe-area behavior.
- Scene list labels use user-facing `Sahne XX · Başlık`; internal SC IDs remain implementation-only.
- Starting a dialogue begins playback automatically and continues line-by-line until paused or complete.
- Dialogue uses layered scene rendering: background + scene depth layer + both characters; the active speaker is visually emphasized without changing character identity.
- Chinese, optional tone-marked pinyin and optional Turkish translation remain visible in the dedicated subtitle card and must never be hidden behind controls/system navigation.
- Bottom controls keep the current two-row mobile-safe concept; do not regress to cramped controls.
- Shadowing UI layout stays as currently accepted. Recording auto-stops after speech followed by silence; Mandarin recognition must work offline without requiring a device speech-recognition service.
- Shadowing shows the recognized Mandarin text plus a factual target-vs-recognized similarity percentage. Do not label text similarity as tone/pronunciation accuracy.
- Existing working behavior is regression-protected. Future scenes must reuse this concept rather than creating one-off screen designs.

