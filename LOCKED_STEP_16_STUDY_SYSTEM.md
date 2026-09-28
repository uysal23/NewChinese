# LOCKED STEP 16 — Study System
Vocabulary cards show Simplified Chinese, pinyin, Turkish, natural audio, example and favorite star. Previous/next arrows exist on front/back. Star toggles add/remove favorite. Sentence modes: reorder, fill blank, choose word, repair order, listen-select. Reorder shows selected tokens above and allows tap-to-return; correct sentence can be heard before practice. Fill-blank uses a wider dashed blank. Telaffuz Çalışması: listen, repeat, compare, retry, next. Choices 6/10/15/Entire dialogue. Show compatibility %, pronunciation %, tone %, fluency % where technically reliable. Dashboard shows overall Pronunciation Accuracy %. Free Study never resets progression.

## Golden Demo V1 Telaffuz Çalışması lock
Telaffuz Çalışması keeps the accepted UI. The learner starts recording with the existing control; after speech begins, approximately one second of silence ends the capture automatically. Recognition must have an offline Mandarin path bundled with the app so it does not depend on Samsung/Google device RecognitionService availability. The result shows recognized Mandarin and a target-vs-recognized text similarity percentage. User recording playback remains available. Tone/pronunciation/fluency percentages may only be added later when they are technically measured rather than inferred.

## 2026-09-27 accepted study-flow refinements
- Sentence practice is derived from the complete dialogue, not a small example subset.
- Every dialogue sentence appears exactly once in each of three required practice modes: reorder, fill-blank, and listening comprehension. Therefore a scene with N dialogue lines exposes N×3 sentence-practice tasks.
- Reorder remains interactive token/chunk ordering: selected parts appear in the answer area and can be tapped to return.
- The Study screen displays vocabulary count, dialogue sentence count, task count, and percentage progress.
- Vocabulary, Sentence Practice, and Telaffuz Çalışması each persist their own 0–100 progress percentage.
- When each of those three study categories reaches at least 50%, the learner may optionally enter the Scene Exam. This only changes exam eligibility; passing rules remain Word Exam >=90% and Sentence Exam >=85% for scene completion/unlock.
- Completing Sentence Practice returns directly to the Study hub so the learner can choose Vocabulary, Telaffuz Çalışması, or another activity.
- Telaffuz Çalışması must show a numeric target-vs-recognized text similarity percentage and persist the best measured similarity. Do not relabel text similarity as pronunciation/tone accuracy.


## 2026-09-28 accepted free-study and pronunciation refinements
- User-facing UI terminology is **Telaffuz Çalışması**. Internal implementation names may retain `shadowing` for backward-compatible code/data fields, but the learner must not see the English label.
- Free Study vocabulary is selected from the chosen dialogue's available vocabulary pool rather than a fixed small card count.
- The learner may choose any subset of available dialogue vocabulary or select all available words before starting.
- Vocabulary cards include an inline pronunciation practice control so the currently visible word can be listened to, recorded, recognized offline and compared against the target text.
