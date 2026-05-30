---
name: ProCollector UI
colors:
  surface: '#f9f9ff'
  surface-dim: '#d0daf2'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f3ff'
  surface-container: '#e8eeff'
  surface-container-high: '#dfe8ff'
  surface-container-highest: '#d9e3fb'
  on-surface: '#111c2d'
  on-surface-variant: '#44474b'
  inverse-surface: '#273143'
  inverse-on-surface: '#ecf0ff'
  outline: '#74777b'
  outline-variant: '#c4c7cb'
  surface-tint: '#556069'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#131d25'
  on-primary-container: '#7b858f'
  inverse-primary: '#bdc8d3'
  secondary: '#3d665b'
  on-secondary: '#ffffff'
  secondary-container: '#bde9db'
  on-secondary-container: '#426a60'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#191c1e'
  on-tertiary-container: '#818487'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d9e4ef'
  primary-fixed-dim: '#bdc8d3'
  on-primary-fixed: '#131d25'
  on-primary-fixed-variant: '#3e4851'
  secondary-fixed: '#c0ecde'
  secondary-fixed-dim: '#a4cfc2'
  on-secondary-fixed: '#00201a'
  on-secondary-fixed-variant: '#254e44'
  tertiary-fixed: '#e0e3e6'
  tertiary-fixed-dim: '#c4c7ca'
  on-tertiary-fixed: '#191c1e'
  on-tertiary-fixed-variant: '#44474a'
  background: '#f9f9ff'
  on-background: '#111c2d'
  surface-variant: '#d9e3fb'
typography:
  headline-md:
    fontFamily: Hanken Grotesk
    fontSize: 20px
    fontWeight: '700'
    lineHeight: 28px
  body-lg:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Hanken Grotesk
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  label-md:
    fontFamily: Hanken Grotesk
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  price-display:
    fontFamily: Hanken Grotesk
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-md-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  space-xs: 4px
  space-sm: 8px
  space-md: 16px
  space-lg: 24px
  component-gap: 12px
  screen-margin: 16px
  card-padding: 16px
---

## Brand & Style

The design system is engineered for a high-utility Trading Card Game (TCG) marketplace. It prioritizes clarity and speed, catering to serious collectors and players who need to parse complex card data quickly. 

The aesthetic is **Corporate / Modern** with a focus on high-information density. It utilizes a neutral palette to ensure that the colorful and diverse artwork of the trading cards remains the focal point. The interface conveys reliability and precision through a structured grid, clear typographic hierarchy, and a restrained use of color accents. The emotional response is intended to be one of trust and professional efficiency, mimicking a high-end inventory management tool rather than a toy store.

## Colors

This design system uses a sophisticated, low-saturation palette to provide a "gallery" feel for the card inventory.

- **Primary:** A deep, near-black navy used for primary text, iconography, and high-emphasis action buttons (like the 'Add' plus button).
- **Secondary:** A muted, organic green inspired by the 'Buy' action. This is reserved for successful states, active toggles, and primary conversion paths.
- **Tertiary/Background:** A range of cool grays and whites. `#F2F4F7` serves as the base for container backgrounds, creating a subtle distinction against the pure white app background.
- **Neutral:** Mid-tone grays used for secondary metadata (set names, collector numbers) and inactive UI states (like damaged/played condition selectors).

## Typography

The design system employs **Hanken Grotesk** for all levels. Its sharp, contemporary geometry provides the precision required for a data-heavy marketplace while remaining highly legible at small sizes.

- **Headlines:** Use Bold weights for card names and section headers.
- **Body:** Standardized on 14px for descriptions. 16px Semibold is used for price labels and primary metadata.
- **Labels:** Use uppercase or tight tracking for card attributes (e.g., "NM", "LP") to distinguish them from narrative text.
- **Price Display:** Specifically treated with high-weight and larger sizing to ensure the primary utility of the app—valuation—is immediate.

## Layout & Spacing

The layout follows a **fluid grid** model optimized for mobile-first interaction. 

- **Grid:** A standard 4-column grid for mobile, expanding to 12 columns for tablet/desktop. 
- **Rhythm:** An 8px base unit drives all spacing. 
- **Margins:** A consistent 16px margin on the left and right of the screen ensures content doesn't feel cramped against device edges.
- **Density:** High density is preferred. Cards should use tight internal padding (16px) to maximize the amount of inventory visible on screen at once. Stacked vertical lists are the primary navigation pattern for browsing cards.

## Elevation & Depth

This design system uses **Tonal Layers** and **Low-Contrast Outlines** rather than heavy shadows to maintain a clean, flat appearance.

- **Level 0:** Pure white (`#FFFFFF`) app background.
- **Level 1:** Surface containers (Card backgrounds) use a subtle border (`1px solid #E4E7EC`) and no shadow. This creates a "sheet" effect that feels integrated into the UI.
- **Level 2:** Floating elements, such as the bottom navigation bar or search bar, utilize a very soft ambient shadow (0px 4px 20px, 5% opacity black) to indicate they sit above the scrollable content.
- **Interactive Depth:** Press states are indicated by a subtle shift in background color (e.g., `#F2F4F7` to `#E4E7EC`) rather than an elevation change.

## Shapes

The shape language is consistently **Rounded** (8px / 0.5rem) to soften the information-heavy interface and make it feel more approachable.

- **Cards:** Use `rounded-lg` (16px) for the main card containers to create a distinct, modern "object" feel.
- **Input Fields/Buttons:** Use the standard 8px radius.
- **Chips/Badges:** Condition indicators (NM, LP) use 4px radius for a sharper, more technical look.
- **Image Containers:** Card art should mirror the card container's roundedness or use a slightly smaller radius (4px-8px) to fit harmoniously within the parent.

## Components

- **Inventory Cards:** The central component. Must include a fixed-aspect-ratio image slot, card name (Headline MD), metadata (Body SM), condition selector (Chips), and a Price/Action cluster.
- **Action Buttons:**
    - **Primary (Add):** Dark background (`#121C24`), white icon, square with 8px radius.
    - **Toggle (Buy/Sell):** Segmented control with the active state using the secondary green (`#76A094`).
- **Condition Chips:** Small, rectangular buttons. Active state uses a light gray fill with bold text; inactive uses a ghost style with muted gray text.
- **Search Bar:** A prominent floating component at the bottom or top. It should be white with a subtle border, containing a search icon and a clear placeholder.
- **Filter Chips:** Horizontal scrolling list of tags (e.g., "All", "Magic: The Gathering") with distinctive brand logos where applicable.