# LOCKED STEP 19 — Integration
Manifest-driven loading; no hard-coded scene content in UI. Bind scene→location, dialogue→speaker→voice→audio, scene→visual manifest, word/sentence IDs to study/exams. Dialogue line changes synchronize active character, audio, Chinese/pinyin/Turkish and speech bubble. Persist favorites, progress, exam results and shadowing scores. Word exam >=90 then sentence exam >=85 unlocks next scene. HSK SC050 unlocks next level SC001. Free Study only lists unlocked scenes. Commerce entitlement remains separate from learning unlock.

## Golden Demo V1 integration lock
All new scenes must plug into the same accepted SC001 flow without UI forks: scene list → intro → auto-playing dialogue → study → vocabulary/sentence/shadowing → gated exams. Scene media/status manifests must truthfully distinguish pending physical assets from complete assets.

## 2026-09-27 accepted admin/progress integration
- Admin Mode is a local access override and must not falsify or reset learner progress records.
- When Admin Mode is enabled, every physically available scene package is accessible from level lists and Free Study regardless of normal unlock state.
- Admin Mode does not create missing content; only scene packages physically present in the app can be opened.
- Normal learner unlock rules remain unchanged when Admin Mode is disabled.
- Free Study remains progression-neutral.
- Partial study percentages for Vocabulary, Sentence Practice and Shadowing are persisted and drive the >=50% Scene Exam eligibility rule.
