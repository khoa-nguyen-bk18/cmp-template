# Design System: ProCollector UI

**Project ID:** 17128375841121903851  
**Stitch asset:** `assets/85dbdd7397814a369b3ac5f21255788b`  
**Platform:** Mobile (Material Design 3 / Jetpack Compose)

## 1. Visual Theme & Atmosphere

A corporate, modern TCG marketplace interface engineered for high information density and collector-grade precision. The mood is clinical yet trustworthy — like a premium inventory tool where colorful card artwork remains the hero. Surfaces use cool tonal layers and whisper-soft outlines instead of heavy shadows, keeping the UI flat, fast to scan, and professional.

- **Density:** High — tight 16px card padding, stacked vertical lists
- **Motion:** Subtle press-state color shifts over elevation changes
- **Roundness:** Consistently rounded (8px base) to soften data-heavy layouts

## 2. Color Palette & Roles

| Role | Name | Hex | Usage |
|------|------|-----|-------|
| Primary | Deep Inventory Navy | `#121C24` | Primary actions, Add button, high-emphasis icons |
| On Primary | Pure White | `#FFFFFF` | Text/icons on primary surfaces |
| Primary Container | Midnight Slate | `#131D25` | Filled tonal containers |
| Secondary | Collector Green | `#3D665B` / accent `#76A094` | Buy/success states, active toggles |
| Secondary Container | Mint Wash | `#BDE9DB` | Secondary tonal backgrounds |
| Background | Cool Canvas | `#F9F9FF` | App background |
| Surface | Cool Canvas | `#F9F9FF` | Primary surface |
| Surface Container | Soft Blue Gray | `#E8EEFF` | Cards, grouped sections |
| Surface Container Highest | Periwinkle Mist | `#D9E3FB` | Elevated tonal layers |
| On Surface | Ink Navy | `#111C2D` | Primary text |
| On Surface Variant | Steel Gray | `#44474B` | Metadata, secondary text |
| Neutral | Utility Gray | `#667085` | Inactive chips, helper labels |
| Tertiary Surface | Gallery Gray | `#F2F4F7` | Container distinction |
| Outline | Mid Gray | `#74777B` | Borders, dividers |
| Outline Variant | Light Gray | `#C4C7CB` | Subtle structural lines |
| Error | Alert Red | `#BA1A1A` | Destructive/error states |

**Dark mode:** Inverse surface `#273143`, inverse on-surface `#ECF0FF`, inverse primary `#BDC8D3`.

## 3. Typography Rules

**Font:** Hanken Grotesk (headline, body, label)

| Token | Size / Line | Weight | M3 mapping |
|-------|-------------|--------|--------------|
| headline-md | 20 / 28 | Bold (700) | `titleLarge` |
| headline-md-mobile | 18 / 24 | Bold (700) | `titleSmall` |
| body-lg | 16 / 24 | SemiBold (600) | `bodyLarge` / `titleMedium` |
| body-md | 14 / 20 | Regular (400) | `bodyMedium` |
| body-sm | 12 / 18 | Regular (400) | `bodySmall` |
| label-md | 12 / 16 | SemiBold (600), +0.02em | `labelMedium` |
| price-display | 24 / 32 | Bold (700) | Custom `AppThemeTypography.priceDisplay` |

Headlines use bold weight for card names and section headers. Labels use tight tracking for condition codes (NM, LP). Price display is always the largest emphasis for valuation.

## 4. Component Stylings

* **Buttons:** 8px radius. Primary uses Deep Inventory Navy fill with white label. Active press shifts background tone, not elevation.
* **Cards:** 16px radius (`large` shape), surface container fill, 1px outline-variant border, no shadow. 16px internal padding.
* **Chips / Badges:** 4px radius (`extraSmall`). Active: light gray fill + bold text. Inactive: ghost with muted neutral.
* **Inputs / Search:** White surface, subtle outline border, 8px radius. Floating search uses soft ambient shadow (Level 2).
* **Segmented controls:** Active segment uses Collector Green accent `#76A094`.

## 5. Layout Principles

- **Grid:** 4-column mobile, 12-column tablet/desktop
- **Rhythm:** 8px base unit — xs=4, sm=8, md=16, lg=24, component-gap=12, screen-margin=16
- **Density:** Prefer vertical stacked lists; maximize visible inventory per screen
- **Touch targets:** Minimum 48dp for interactive elements
- **Elevation:** Tonal layers over shadows; Level 2 shadow only for floating nav/search

## 6. Shape Language

| Token | Radius | Usage |
|-------|--------|-------|
| sm | 4dp | Chips, condition badges |
| default | 8dp | Buttons, inputs |
| md | 12dp | Medium containers |
| lg | 16dp | Inventory cards |
| xl | 24dp | Sheets, prominent containers |
| full | pill | Avatars, filter pills |

## 7. Compose Implementation

Theme files live in `shared/src/commonMain/kotlin/com/devindie/cmptemplate/ui/theme/`:

- `Color.kt` — light/dark `ColorScheme` from Stitch named colors
- `Type.kt` — M3 typography + `PriceDisplayStyle`
- `Shape.kt` — M3 `Shapes` from Stitch roundness tokens
- `Spacing.kt` — `AppSpacing` via `LocalAppSpacing`
- `Theme.kt` — `AppTheme` composable wrapping `MaterialTheme`

Wrap screens with `AppTheme { }` and read tokens via `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`, and `LocalAppSpacing.current`.
