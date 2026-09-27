# LOCKED STEP 18 — Audio System
Natural Standard Mandarin. Persistent voice per character. Dialogue, word, sentence, shadowing reference and correct-sentence audio. Main learning audio must not use phone TTS. Natural rhythm and emotion, no robotic prosody. User playback speeds: 0.75x/0.85x/1.0x/1.15x/1.25x. M4A/AAC preferred. Normalize levels, trim unnecessary silence, validate clipping/noise/voice consistency. Shadowing compares user recording with reference and supports compatibility/pronunciation/tone/fluency metrics where reliable.

## Golden Demo V1 speech lock
Reference dialogue audio remains persistent-voice Mandarin assets. Shadowing recognition is separate from reference TTS and must support bundled offline Mandarin recognition. Recording ends automatically after post-speech silence and preserves the learner recording for replay. Text similarity is reported honestly as similarity, not as tone accuracy.

## Locked production engine and encoding
For all core NewChinese Mandarin learning audio, use the existing repository production pipeline unless the user explicitly approves a migration:
- Production tool: `edge-tts` through `tools/generate_audio_assets.py`.
- Li Na / `VOICE_LI_NA_001`: `zh-CN-XiaoxiaoNeural`.
- Zhang Wei / `VOICE_ZHANG_WEI_001`: `zh-CN-YunxiNeural`.
- Output container/codec: M4A / AAC.
- Encoding: AAC 128 kbps, 44.1 kHz, mono.
- Loudness normalization: FFmpeg `loudnorm=I=-18:TP=-2:LRA=7`.
- Source speaking rates: dialogue `+0%`, vocabulary `-8%`, sentence/reference `-3%`.
- Pitch: `+0Hz` for both locked character voices.
- Production requirements: Standard Mandarin, natural conversational prosody, no music, no SFX, normalize loudness, trim unnecessary silence, reject clipping/noise/wrong text/wrong voice.
- Phone/device TTS is prohibited for core learning/reference audio.
- Per-character voice identity is persistent across every HSK level.
- Audio may be marked complete only after every declared physical M4A exists and validation passes.

