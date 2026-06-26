# Maestro E2E tests

End-to-end UI tests for the CMP Template Android app using [Maestro](https://maestro.mobile.dev/).

## Prerequisites

- [Maestro CLI](https://maestro.mobile.dev/getting-started/installing-maestro) installed
- Android device or emulator with USB debugging enabled
- Debug APK installed on the target device

## Quick start

```bash
# List connected devices
maestro list-devices

# Build and install debug APK
./gradlew :androidApp:installDebug

# Run all BrowseScreen flows
maestro test maestro/flows/browse/ --device <DEVICE_ID>

# Run a single flow
maestro test maestro/flows/browse/browse_smoke.yaml --device <DEVICE_ID>

# Smoke tests only
maestro test maestro/flows/browse/ --device <DEVICE_ID> --include-tags smoke
```

Default device in [config.yaml](config.yaml): `R5CRC19752K`.

## Flow layout

```
maestro/
  config.yaml
  subflows/
    _browse_setup.yaml          # shared launch + catalog wait (not run directly)
  flows/
    browse/
      browse_smoke.yaml         # launch, store name, catalog visible
      browse_search.yaml        # debounced search + empty state
      browse_category_filter.yaml
      browse_card_detail.yaml   # tap card row → detail → back
      browse_scroll_to_top.yaml
      browse_pull_to_refresh.yaml
      browse_pagination.yaml
```

Subflows live outside `flows/` so Maestro does not execute them as standalone tests.

## Selector conventions

Maestro flows target **accessibility text** from Compose semantics:

| UI element | Selector |
|------------|----------|
| Search field | `"Search inventory"` |
| Category chips | `"All filter"`, `"Magic filter"`, etc. |
| Card row (tap target) | `"{name}, {price}"` e.g. `"Gaeas Touch, $5.62"` |
| Card detail top bar | `"Back"` |
| Scroll-to-top FAB | `"Scroll to top"` |
| Store name | `"Good Games Belconnen"` |

Compose `testTag`s (`browse_screen`, `browse_search_field`, etc.) are kept in source for Compose UI tests but are not used in Maestro flows on Android because they are not reliably exposed as Maestro `id` selectors.

## Debugging

```bash
# Inspect live hierarchy before authoring selectors
maestro hierarchy --device <DEVICE_ID>

# View test recordings and screenshots after a failure
open ~/.maestro/tests/
```

Maestro MCP is configured in [`.cursor/mcp.json`](../.cursor/mcp.json) for agent-driven iteration (`inspect_screen`, `run`, `take_screenshot`).

## Relationship to unit tests

| Layer | Tool | Location |
|-------|------|----------|
| ViewModel state / debounce | kotlin-test | `shared/src/commonTest/.../BrowseViewModelTest.kt` |
| Integrated browse journeys | Maestro | `maestro/flows/browse/` |

Maestro is **not** part of `./gradlew qualityCheck`. Run it manually before release or when changing Browse UI.
