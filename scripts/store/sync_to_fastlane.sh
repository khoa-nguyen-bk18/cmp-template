#!/usr/bin/env bash
# Copy store/metadata and store/output into Fastlane supply/deliver paths.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

LOCALE="${1:-en-US}"

ANDROID_META_SRC="store/metadata/${LOCALE}/android"
IOS_META_SRC="store/metadata/${LOCALE}/ios"
ANDROID_SHOTS_SRC="store/output/${LOCALE}/android/phoneScreenshots"
IOS_SHOTS_SRC="store/output/${LOCALE}/ios/6.9-inch"

ANDROID_META_DST="androidApp/fastlane/metadata/android/${LOCALE}"
IOS_META_DST="iosApp/fastlane/metadata/${LOCALE}"
ANDROID_SHOTS_DST="${ANDROID_META_DST}/images/phoneScreenshots"
IOS_SHOTS_DST="iosApp/fastlane/screenshots/${LOCALE}"

copy_text_files() {
  local src="$1"
  local dst="$2"
  mkdir -p "$dst"
  if [[ -d "$src" ]]; then
    cp -f "$src"/*.txt "$dst/" 2>/dev/null || true
  fi
}

copy_text_files "$ANDROID_META_SRC" "$ANDROID_META_DST"
copy_text_files "$IOS_META_SRC" "$IOS_META_DST"

mkdir -p "$ANDROID_SHOTS_DST"
if [[ -d "$ANDROID_SHOTS_SRC" ]]; then
  cp -f "$ANDROID_SHOTS_SRC"/*.png "$ANDROID_SHOTS_DST/" 2>/dev/null || true
fi

mkdir -p "$IOS_SHOTS_DST"
if [[ -d "$IOS_SHOTS_SRC" ]]; then
  cp -f "$IOS_SHOTS_SRC"/*.png "$IOS_SHOTS_DST/" 2>/dev/null || true
fi

CHANGELOG="androidApp/fastlane/metadata/android/${LOCALE}/changelogs/default.txt"
mkdir -p "$(dirname "$CHANGELOG")"
if [[ ! -f "$CHANGELOG" ]]; then
  echo "Bug fixes and improvements." > "$CHANGELOG"
fi

echo "Synced store assets for locale '${LOCALE}' to Fastlane paths."
