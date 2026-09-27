# LOCKED STEP 16 — Study System
Vocabulary cards show Simplified Chinese, pinyin, Turkish, natural audio, example and favorite star. Previous/next arrows exist on front/back. Star toggles add/remove favorite. Sentence modes: reorder, fill blank, choose word, repair order, listen-select. Reorder shows selected tokens above and allows tap-to-return; correct sentence can be heard before practice. Fill-blank uses a wider dashed blank. Shadowing: listen, repeat, compare, retry, next. Choices 6/10/15/Entire dialogue. Show compatibility %, pronunciation %, tone %, fluency % where technically reliable. Dashboard shows overall Pronunciation Accuracy %. Free Study never resets progression.

## Golden Demo V1 Shadowing lock
Shadowing keeps the accepted UI. The learner starts recording with the existing control; after speech begins, approximately one second of silence ends the capture automatically. Recognition must have an offline Mandarin path bundled with the app so it does not depend on Samsung/Google device RecognitionService availability. The result shows recognized Mandarin and a target-vs-recognized text similarity percentage. User recording playback remains available. Tone/pronunciation/fluency percentages may only be added later when they are technically measured rather than inferred.

## 2026-09-27 accepted study-flow refinements
- Sentence practice is derived from the complete dialogue, not a small example subset.
- Every dialogue sentence appears exactly once in each of three required practice modes: reorder, fill-blank, and listening comprehension. Therefore a scene with N dialogue lines exposes N×3 sentence-practice tasks.
- Reorder remains interactive token/chunk ordering: selected parts appear in the answer area and can be tapped to return.
- The Study screen displays vocabulary count, dialogue sentence count, task count, and percentage progress.
- Vocabulary, Sentence Practice, and Shadowing each persist their own 0–100 progress percentage.
- When each of those three study categories reaches at least 50%, the learner may optionally enter the Scene Exam. This only changes exam eligibility; passing rules remain Word Exam >=90% and Sentence Exam >=85% for scene completion/unlock.
- Completing Sentence Practice returns directly to the Study hub so the learner can choose Vocabulary, Shadowing, or another activity.
- Shadowing must show a numeric target-vs-recognized text similarity percentage and persist the best measured similarity. Do not relabel text similarity as pronunciation/tone accuracy.
