# GitHub-only development workflow

NewChinese is designed so development, validation and Android builds can run from GitHub without requiring Codex or Android Studio on the user's computer.

## Normal development flow

1. Source files are edited directly in the GitHub repository.
2. `.github/workflows/validate-build.yml` validates content, runs unit tests and Android lint.
3. GitHub Actions builds:
   - Debug APK
   - unsigned Release APK
   - unsigned Release AAB
4. Build outputs are stored as GitHub Actions artifacts.

## Media flow

Temporary visual/audio packages are uploaded only to:

`incoming_assets/`

Package naming:

`HSK1_SC001_visual_assets.zip`

or:

`HSK1_SC001_audio_assets.zip`

The media import workflow moves approved files to the scene's declared `assets/` directory, checks `media_status.json`, removes the temporary ZIP and commits the result.

A media category cannot become `complete` unless every physical asset declared by that scene exists.

## Locked project structure

The existing 20 locked step manifests remain the source of truth. This GitHub-only workflow adds automation around the existing project and does not replace or weaken the locked requirements.

## Release signing

Debug and unsigned release builds require no local computer.

A store-ready signed release will later require one-time signing credentials / Play App Signing configuration through GitHub Secrets or Google Play. Secrets must never be committed to this repository.
