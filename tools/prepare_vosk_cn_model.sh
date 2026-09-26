#!/usr/bin/env bash
set -euo pipefail

MODEL_NAME="vosk-model-small-cn-0.22"
MODEL_URL="https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip"
DEST_ROOT="content/models"
DEST_DIR="$DEST_ROOT/$MODEL_NAME"
TMP_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

if [[ -f "$DEST_DIR/am/final.mdl" && -f "$DEST_DIR/conf/mfcc.conf" ]]; then
  echo "Vosk Mandarin model already prepared."
  exit 0
fi

rm -rf "$DEST_DIR"
mkdir -p "$DEST_ROOT"

echo "Downloading $MODEL_NAME..."
curl --fail --location --retry 4 --retry-delay 2 \
  "$MODEL_URL" \
  --output "$TMP_DIR/$MODEL_NAME.zip"

echo "Extracting $MODEL_NAME..."
unzip -q "$TMP_DIR/$MODEL_NAME.zip" -d "$TMP_DIR"

if [[ ! -f "$TMP_DIR/$MODEL_NAME/am/final.mdl" ]]; then
  echo "Invalid Vosk model: missing am/final.mdl" >&2
  exit 1
fi
if [[ ! -f "$TMP_DIR/$MODEL_NAME/conf/mfcc.conf" ]]; then
  echo "Invalid Vosk model: missing conf/mfcc.conf" >&2
  exit 1
fi

mv "$TMP_DIR/$MODEL_NAME" "$DEST_DIR"

echo "Prepared $DEST_DIR"
du -sh "$DEST_DIR"
