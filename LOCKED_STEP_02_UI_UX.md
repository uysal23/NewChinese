# LOCKED STEP 02 — UI/UX
Dashboard: personalized greeting, HSK Levels, Placement Test, Free Study, Favorites, Progress, Settings, pronunciation accuracy metric. HSK levels are reached from one button. Dialogue is full screen with Study top-left and Pinyin top-right, Turkish subtitle toggle, start/pause/previous/next. Study contains Vocabulary, Sentence, Shadowing. Settings include user name, light/dark, four pastel palettes, speech speed, defaults for pinyin/translation, reminders with custom/default message. Learning screens keep the phone display awake; backgrounding restores normal sleep behavior.

## Golden Demo V1 UI lock
The accepted SC001 phone layout is now the reference UI. Preserve its safe-area handling, dialogue subtitle card, two-row mobile controls, automatic dialogue playback, user-facing `Sahne XX · Başlık` labels, and current Shadowing screen arrangement. New scenes reuse this UI rather than introducing scene-specific layouts.

## 2026-09-27 accepted UI refinements
- The bottom navigation Home control must always return to the Dashboard reliably.
- Pastel palette controls must keep normal button/chip proportions on narrow phones; use a multi-row layout rather than squeezing PEACH or other palettes.
- While the app is in the foreground, keep the phone display awake globally. When the app is backgrounded, normal device sleep behavior resumes.
- Progress cards must never force values such as "Henüz veri yok" into single-character vertical wrapping; label/value areas remain readable on narrow screens.
- Settings include an Admin Access section. Entering the exact password `HSK2026` enables Admin Mode; Admin Mode can also be disabled from Settings.
