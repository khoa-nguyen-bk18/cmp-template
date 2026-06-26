# Store publishing pipeline

Step-by-step guide for listing metadata, screenshot capture, validation, and upload to **Google Play (internal track)** and **TestFlight**. This pipeline is part of the **App Factory** template: every generated app keeps the same layout; only `app.factory.yaml`, metadata copy, and branding templates change.

**Design spec:** [docs/superpowers/specs/2026-06-26-store-publishing-pipeline-design.md](../docs/superpowers/specs/2026-06-26-store-publishing-pipeline-design.md)

---

## What you get at the end

| Output | Where it goes |
|--------|----------------|
| Validated listing copy | `store/metadata/{locale}/` → Play Console / App Store Connect |
| Framed screenshots | `store/output/{locale}/android/` and `.../ios/` |
| Signed **AAB** | Play Console → **Internal testing** |
| Signed **IPA** | App Store Connect → **TestFlight** |

v1 does **not** promote to production. That is a later phase.

---

## Implementation status

| Phase | Status | What works |
|-------|--------|------------|
| 1 — Layout, config, validate | **Done** | `validate_store.sh`, `check-prerequisites.sh`, `store-validate.yml` CI |
| 2 — Capture + framing | **Done** | `capture.sh`, Maestro flows, `frame_assets.py` |
| 3 — Fastlane upload lanes | **Done** | `android beta_internal`, `ios beta_testflight`, `sync_to_fastlane.sh` |
| 4 — CI internal release | **Done** | `store-release-internal.yml` (requires GitHub secrets for upload) |

**Quick start (metadata only):**

```bash
./scripts/store/setup-python.sh
./scripts/store/validate_store.sh --locale en-US --metadata-only --skip-gplay
```

**Full local pipeline (Android):**

```bash
./scripts/store/check-prerequisites.sh
./gradlew :androidApp:installDebug
./scripts/store/capture.sh --platform android --device <DEVICE_ID>
python3 scripts/store/frame_assets.py --locale en-US
./scripts/store/validate_store.sh --locale en-US --skip-gplay
bundle install
cd androidApp && bundle exec fastlane android beta_internal
```

---

## Prerequisites

### All developers (metadata + validate)

| Tool | Purpose | Install |
|------|---------|---------|
| **Git** | Source control | System |
| **jq** | JSON in shell scripts | `brew install jq` |
| **`gplay` CLI** | Play listing/screenshot/bundle validation | `brew tap tamtom/tap && brew install tamtom/tap/gplay` — [play-console-cli](https://github.com/tamtom/play-console-cli) |
| **Python 3.10+** | Screenshot framing | `./scripts/store/setup-python.sh` (creates `scripts/store/.venv`) |
| **Pillow + PyYAML** | Image compositing / YAML | Installed by `setup-python.sh` |

### Android capture + release

| Tool | Purpose | Install |
|------|---------|---------|
| **JDK 17+** | Builds | Android Studio / `brew install openjdk@17` |
| **Android SDK + emulator** | AAB build, Maestro capture | Android Studio → SDK Manager |
| **Maestro CLI** | UI screenshot flows | [Maestro install](https://maestro.mobile.dev/getting-started/installing-maestro) |
| **Ruby 3 + Bundler + Fastlane** | Play upload *(Phase 3)* | `brew install ruby` → `bundle install` in repo root |

### iOS capture + release (macOS only)

| Tool | Purpose | Install |
|------|---------|---------|
| **Xcode** | Simulator, IPA, TestFlight | Mac App Store |
| **Xcode Command Line Tools** | `xcodebuild` | `xcode-select --install` |
| **Maestro CLI** | iOS capture flows | Same as Android |
| **Fastlane + match** | TestFlight upload, signing *(Phase 3)* | `bundle install` |

### Check prerequisites

*(Phase 1)*

```bash
./scripts/store/check-prerequisites.sh
```

Fix anything reported as missing before continuing.

---

## One-time store account setup

Do this once per app (or per generated app from the factory).

### Google Play

1. Create the app in [Google Play Console](https://play.google.com/console).
2. Complete **App content** sections (privacy policy, data safety, content rating) — uploads can fail without these.
3. Create a **service account** with **Release manager** (or admin) access:
   - Google Cloud Console → IAM → Service account → Create key (JSON).
   - Play Console → Users and permissions → Invite service account.
4. Save the JSON path locally (never commit):
   ```bash
   export GPLAY_SERVICE_ACCOUNT=/path/to/play-service-account.json
   # or: gplay auth login
   ```
5. Create an **upload keystore** for release signing (keep backup; losing it blocks updates):
   ```bash
   keytool -genkey -v -keystore release.keystore -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```
6. Register the upload key in Play Console → App signing.

### Apple App Store

1. Create the app in [App Store Connect](https://appstoreconnect.apple.com).
2. Note **Apple ID** (numeric) for the app — needed for share URLs and deliver.
3. Create an **App Store Connect API key** (Admin or App Manager):
   - Users and Access → Keys → App Store Connect API.
   - Download `.p8` once; store Key ID and Issuer ID.
4. Set up **fastlane match** *(Phase 3)* for App Store distribution certs (private git repo for certificates).
5. Prepare **App Review** demo account notes if login is required.

### GitHub Actions secrets *(Phase 4)*

| Secret | Value |
|--------|--------|
| `GPLAY_SERVICE_ACCOUNT_JSON` | Full JSON contents |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded keystore |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias |
| `ANDROID_KEY_PASSWORD` | Key password |
| `ASC_KEY_ID` | API key ID |
| `ASC_ISSUER_ID` | Issuer ID |
| `ASC_KEY_CONTENT` | `.p8` contents |
| `MATCH_PASSWORD` | match encryption password |
| `MATCH_GIT_URL` | Private certs repo URL |

---

## Configure your app

### 1. Edit factory config

`store/app.factory.yaml` is the single contract for identity, branding, locales, and capture devices.

```yaml
app:
  displayName: "My App"
  androidPackage: "com.example.myapp"
  iosBundleId: "com.example.myapp"

branding:
  primaryColor: "#6750A4"
  frameTemplate: "templates/phone_frame.png"

locales:
  default: en-US
  supported: [en-US]

capture:
  android:
    emulator: "Pixel_7_API_34"
    flow: "capture/flows/android.yaml"
  ios:
    simulator: "iPhone 16 Pro"
    flow: "capture/flows/ios.yaml"

screenshots:
  slots: 5
  devices:
    android: phone
    ios: "6.9-inch"

release:
  androidTrack: internal
  iosDestination: testflight
```

**App Factory:** when codegen creates a new app, this file is rewritten together with Gradle `applicationId` and iOS bundle ID.

### 2. Edit listing copy (`en-US`)

```
store/metadata/en-US/
├── android/
│   ├── title.txt              # max 30 chars
│   ├── short_description.txt  # max 80 chars
│   └── full_description.txt   # max 4000 chars
├── ios/
│   ├── name.txt               # max 30 chars
│   ├── subtitle.txt           # max 30 chars
│   ├── keywords.txt           # max 100 chars, comma-separated, no spaces after commas
│   ├── description.txt
│   └── promotional_text.txt   # max 170 chars
└── captions.yaml              # Screenshot captions (benefit-oriented, not feature labels)
```

Example `captions.yaml`:

```yaml
slots:
  - id: home
    above: "Browse your collection"
    below: "Filter and search in seconds"
  - id: detail
    above: "Rich card details"
    below: "Everything in one place"
```

### 3. Branding templates

- `store/templates/phone_frame.png` — device bezel overlay
- `store/templates/caption_style.yaml` — font size, position, colors (uses `branding.primaryColor`)
- Optional: `store/templates/feature_graphic/` for Play feature graphic (1024×500)

---

## Step-by-step workflows

### Workflow A — Validate metadata only (no device)

Use when editing copy before a release.

```bash
# Play listing rules (title length, required fields, UTF-8)
gplay validate listing --dir store/metadata --locale en-US

# Play screenshot folder rules (after outputs exist)
gplay validate screenshots --dir store/output --locale en-US

# Project wrapper (Play + iOS char limits, dimensions)
./scripts/store/validate_store.sh --locale en-US    # *(Phase 1)*
```

**Expected result:** exit code `0`; no errors in terminal. Fix any reported field before upload.

---

### Workflow B — Capture and frame screenshots

Use when UI changed or you need fresh store images.

#### B1. Start emulator / simulator

**Android**

```bash
# List AVDs
emulator -list-avds

# Start (name from app.factory.yaml)
emulator -avd Pixel_7_API_34 &

# Build and install debug build for Maestro
./gradlew :androidApp:installDebug
```

**iOS** (macOS)

```bash
# Open simulator from Xcode or:
open -a Simulator

# Build and run iosApp from Xcode, or your KMP iOS build task
```

#### B2. Run Maestro capture flows

*(Phase 2)*

```bash
maestro test store/capture/flows/android.yaml --device <DEVICE_ID>
maestro test store/capture/flows/ios.yaml --device <SIMULATOR_UDID>
```

**Expected raw output:**

```
store/.raw/android/01_home.png
store/.raw/android/02_detail.png
...
store/.raw/ios/01_home.png
...
```

#### B3. Frame and caption

*(Phase 2)*

```bash
./scripts/store/setup-python.sh
./scripts/store/.venv/bin/python3 scripts/store/frame_assets.py \
  --locale en-US \
  --config store/app.factory.yaml
```

**Expected framed output:**

```
store/output/en-US/android/phoneScreenshots/01_home.png
store/output/en-US/ios/6.9-inch/01_home.png
...
```

#### B4. Validate assets

```bash
gplay validate screenshots --dir store/output --locale en-US
./scripts/store/validate_store.sh --locale en-US
```

**Screenshot requirements (compliance):**

| Store | Requirement |
|-------|-------------|
| Google Play phone | Min **2**, max **8** PNG/JPEG; min 320px on short side |
| App Store iPhone | **6.9-inch** set required (primary); show **actual app UI**, not marketing mockups only |
| App Store iPad | **13-inch** set if app runs on iPad |

---

### Workflow C — Build release binaries

*(Phase 3 — local signing required)*

**Android AAB**

```bash
./gradlew :androidApp:bundleRelease
# Output: androidApp/build/outputs/bundle/release/*.aab
```

**iOS IPA**

```bash
cd iosApp
bundle exec fastlane ios build_release   # lane name TBD in Phase 3
```

Validate Android bundle before upload:

```bash
gplay validate bundle --file androidApp/build/outputs/bundle/release/*.aab
```

---

### Workflow D — Upload to internal / TestFlight

*(Phase 3 — local)*

```bash
# Android → Play internal track (metadata + screenshots + AAB)
cd androidApp
bundle exec fastlane android beta_internal

# iOS → TestFlight
cd iosApp
bundle exec fastlane ios beta_testflight
```

**Expected result:**

- Play Console → **Internal testing** shows new release with listing text and screenshots
- App Store Connect → **TestFlight** shows new build processing (then available to internal testers)

Dry-run before first real upload *(recommended)*:

```bash
gplay release \
  --package com.example.myapp \
  --track internal \
  --bundle path/to/app.aab \
  --listings-dir store/metadata \
  --screenshots-dir store/output \
  --dry-run
```

---

### Workflow E — CI internal release

*(Phase 4)*

1. Open GitHub → **Actions** → **Store release (internal)**.
2. Click **Run workflow**.
3. Set inputs:
   - `confirm`: `true`
   - `locale`: `en-US` (default)
   - Optional: skip capture if screenshots unchanged
4. Wait for job completion.

**Expected result:** same as Workflow D, artifacts uploaded from CI secrets.

---

## Adding a new locale later

1. Copy `store/metadata/en-US/` → `store/metadata/vi/` (or `ja`, etc.).
2. Translate all `.txt` files and `captions.yaml` (research keywords per market — do not machine-translate keywords blindly).
3. Add locale to `app.factory.yaml`:
   ```yaml
   locales:
     default: en-US
     supported: [en-US, vi]
   ```
4. Re-run capture if the app UI is localized (Maestro flows may need locale-specific subflows).
5. Re-run `frame_assets.py` and validation for each locale.
6. Upload includes all configured locales in Fastlane supply/deliver step.

---

## Expected file tree (after full run)

```
store/
├── app.factory.yaml
├── metadata/en-US/...
├── capture/flows/...
├── templates/...
├── .raw/                          # gitignored
│   ├── android/*.png
│   └── ios/*.png
└── output/                        # gitignored
    └── en-US/
        ├── android/
        │   ├── phoneScreenshots/
        │   └── images/featureGraphic.png   # optional
        └── ios/
            └── 6.9-inch/
```

---

## Troubleshooting

| Problem | Likely cause | Fix |
|---------|--------------|-----|
| `title exceeds 30 characters` | Play / App Store name too long | Shorten `title.txt` / `name.txt` |
| `keywords invalid` | Spaces after commas or duplicates of title words | Use `word,word,word` only |
| Screenshots rejected (Apple) | Marketing mockups, not real UI | Re-capture from simulator; keep frames but show real screens |
| `Version code already exists` | `versionCode` not bumped | Increment in `androidApp/build.gradle.kts` |
| Signing key mismatch | Wrong keystore vs Play upload key | Use registered upload key |
| Maestro flow fails | UI text or IDs changed | Update `store/capture/flows/*.yaml`; see [maestro/README.md](../maestro/README.md) |
| TestFlight stuck processing | Normal delay | Wait 5–30 min; check email for compliance issues |
| Data safety / privacy blocks Play | Incomplete forms | Finish Play Console → App content |
| iOS upload requires Xcode 26+ *(2026+)* | Old Xcode | Upgrade per Apple upload requirements |

---

## Quick reference commands

```bash
# Prerequisites
./scripts/store/check-prerequisites.sh

# Validate copy
gplay validate listing --dir store/metadata --locale en-US

# Capture → frame → validate
maestro test store/capture/flows/android.yaml --device <ID>
python3 scripts/store/frame_assets.py --locale en-US --config store/app.factory.yaml
./scripts/store/validate_store.sh --locale en-US

# Upload internal
bundle exec fastlane android beta_internal
bundle exec fastlane ios beta_testflight
```

---

## Related docs

- [Maestro E2E tests](../maestro/README.md) — device setup and flow debugging
- [Billing module](../billing/README.md) — Play / ASC product setup (separate from listing pipeline)
- [App promotion feature](../shared/src/commonMain/kotlin/com/devindie/cmptemplate/feature/apppromotion/api/README.md) — in-app store URLs

---

## Checklist before first internal upload

- [ ] `app.factory.yaml` package/bundle IDs match Gradle / Xcode
- [ ] Play app created; data safety and content rating complete
- [ ] App Store Connect app created; API key configured
- [ ] `en-US` metadata filled; `gplay validate listing` passes
- [ ] At least 2 Android phone screenshots; iOS 6.9-inch set complete
- [ ] Screenshots show real app UI
- [ ] Release AAB/IPA signed with correct keys
- [ ] `versionCode` / build number higher than any previous upload
- [ ] Demo account in App Review notes (if login required)
