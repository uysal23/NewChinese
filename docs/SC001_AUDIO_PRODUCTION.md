# HSK1 SC001 — Audio Production Contract

## Locked rules

- Language: Standard Mandarin (zh-CN)
- Format: M4A/AAC preferred
- Natural conversational rhythm; no robotic prosody
- No phone-default TTS for core learning audio
- Zhang Wei always uses VOICE_ZHANG_WEI_001
- Li Na always uses VOICE_LI_NA_001
- Normalize loudness across all files
- Trim unnecessary leading/trailing silence
- Reject clipping, obvious background noise, wrong speaker identity, or wrong text
- App playback speed remains selectable at 0.75x / 0.85x / 1.0x / 1.15x / 1.25x without changing source files

## Upload package

ZIP name:

`HSK1_SC001_audio_assets.zip`

Upload location:

`incoming_assets/HSK1_SC001_audio_assets.zip`

The automatic media importer will copy approved files to:

`content/hsk1/sc001/assets/`

Audio status becomes `complete` only when all 26 physical files exist.

## Dialogue — 18 files

| File | Voice | Mandarin |
|---|---|---|
| hsk1_sc001_line_001.m4a | VOICE_LI_NA_001 | 你好，你是张伟吧？ |
| hsk1_sc001_line_002.m4a | VOICE_ZHANG_WEI_001 | 对，我是张伟。你是李娜吗？ |
| hsk1_sc001_line_003.m4a | VOICE_LI_NA_001 | 是我。终于见到你了！ |
| hsk1_sc001_line_004.m4a | VOICE_ZHANG_WEI_001 | 我也是。谢谢你来接我。 |
| hsk1_sc001_line_005.m4a | VOICE_LI_NA_001 | 不客气。路上顺利吗？ |
| hsk1_sc001_line_006.m4a | VOICE_ZHANG_WEI_001 | 挺顺利的，就是有一点累。 |
| hsk1_sc001_line_007.m4a | VOICE_LI_NA_001 | 坐了很久的车吧？ |
| hsk1_sc001_line_008.m4a | VOICE_ZHANG_WEI_001 | 对，今天早上很早就出发了。 |
| hsk1_sc001_line_009.m4a | VOICE_LI_NA_001 | 你的行李多吗？ |
| hsk1_sc001_line_010.m4a | VOICE_ZHANG_WEI_001 | 不多，就这个箱子和一个包。 |
| hsk1_sc001_line_011.m4a | VOICE_LI_NA_001 | 好，我帮你拿这个包吧。 |
| hsk1_sc001_line_012.m4a | VOICE_ZHANG_WEI_001 | 不用了，我自己可以。谢谢。 |
| hsk1_sc001_line_013.m4a | VOICE_LI_NA_001 | 那我们先出去吧，这里有点吵。 |
| hsk1_sc001_line_014.m4a | VOICE_ZHANG_WEI_001 | 好。外面怎么走？ |
| hsk1_sc001_line_015.m4a | VOICE_LI_NA_001 | 从这边走，出口不远。 |
| hsk1_sc001_line_016.m4a | VOICE_ZHANG_WEI_001 | 好，我跟你走。 |
| hsk1_sc001_line_017.m4a | VOICE_LI_NA_001 | 出去以后我们先喝点水，然后回家。 |
| hsk1_sc001_line_018.m4a | VOICE_ZHANG_WEI_001 | 好啊。那我们走吧。 |

## Vocabulary — 6 files

| File | Voice | Mandarin |
|---|---|---|
| hsk1_sc001_word_001.m4a | VOICE_LI_NA_001 | 你好 |
| hsk1_sc001_word_002.m4a | VOICE_ZHANG_WEI_001 | 是 |
| hsk1_sc001_word_003.m4a | VOICE_ZHANG_WEI_001 | 谢谢 |
| hsk1_sc001_word_004.m4a | VOICE_LI_NA_001 | 见 |
| hsk1_sc001_word_005.m4a | VOICE_ZHANG_WEI_001 | 累 |
| hsk1_sc001_word_006.m4a | VOICE_ZHANG_WEI_001 | 好 |

## Correct sentence audio — 2 files

| File | Voice | Mandarin |
|---|---|---|
| hsk1_sc001_sentence_001.m4a | VOICE_ZHANG_WEI_001 | 谢谢你来接我。 |
| hsk1_sc001_sentence_002.m4a | VOICE_ZHANG_WEI_001 | 我是张伟。 |

## Quality gate

Before audio is accepted:

1. Filename must exactly match the table above.
2. Spoken text must exactly match the locked Mandarin text.
3. Voice identity must match the assigned Voice ID.
4. No clipping or obvious noise.
5. No music or sound effects mixed into learning audio.
6. Dialogue emotion may be natural but must not distort pronunciation.
7. All 26 files must be physically present before `audio.status = complete`.
