---
name: Apex Signage 10-Foot Experience
colors:
  surface: '#171214'
  surface-dim: '#171214'
  surface-bright: '#3e373a'
  surface-container-lowest: '#120d0f'
  surface-container-low: '#201a1c'
  surface-container: '#241e20'
  surface-container-high: '#2f282b'
  surface-container-highest: '#3a3336'
  on-surface: '#ebdfe3'
  on-surface-variant: '#d2c2c9'
  inverse-surface: '#ebdfe3'
  inverse-on-surface: '#352f31'
  outline: '#9b8d93'
  outline-variant: '#4f4449'
  surface-tint: '#ebb8d5'
  primary: '#ebb8d5'
  on-primary: '#48253b'
  primary-container: '#b2839e'
  on-primary-container: '#401e34'
  inverse-primary: '#7b526b'
  secondary: '#debece'
  on-secondary: '#3f2a37'
  secondary-container: '#57404e'
  on-secondary-container: '#ccadbd'
  tertiary: '#d8bceb'
  on-tertiary: '#3c284d'
  tertiary-container: '#a087b3'
  on-tertiary-container: '#352146'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffd8ec'
  primary-fixed-dim: '#ebb8d5'
  on-primary-fixed: '#301026'
  on-primary-fixed-variant: '#613b52'
  secondary-fixed: '#fbd9eb'
  secondary-fixed-dim: '#debece'
  on-secondary-fixed: '#281522'
  on-secondary-fixed-variant: '#57404e'
  tertiary-fixed: '#f1daff'
  tertiary-fixed-dim: '#d8bceb'
  on-tertiary-fixed: '#261236'
  on-tertiary-fixed-variant: '#533e65'
  background: '#171214'
  on-background: '#ebdfe3'
  surface-variant: '#3a3336'
typography:
  display-tv:
    fontFamily: Work Sans
    fontSize: 56px
    fontWeight: '700'
    lineHeight: 68px
    letterSpacing: -0.02em
  headline-xl:
    fontFamily: Work Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Work Sans
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-md:
    fontFamily: Work Sans
    fontSize: 26px
    fontWeight: '600'
    lineHeight: 34px
  body-xl:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 32px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 26px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-tv-focus:
    fontFamily: Work Sans
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
    letterSpacing: 0.04em
  label-key-legend:
    fontFamily: Work Sans
    fontSize: 15px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.05em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-tv: 2rem
  margin: 3rem
  margin-overscan: 4rem
  space-xs: 0.5rem
  space-sm: 0.75rem
  space-md: 1.25rem
  space-lg: 2rem
  space-xl: 3rem
---

## Brand & Style

The design system is engineered specifically for digital signage management and television kiosk playback on large-format 1080p displays running Android TV environments. Designed around the physical realities of the 10-foot user experience (viewing distance of 2 to 3 meters), the interface emphasizes immediate cognitive clarity, rapid spatial comprehension via directional remote controls (D-Pad navigation), and high-contrast ergonomics.

The visual style blends modern corporate utility with high-visibility architectural precision. Because older hardware panels (such as Android 6 SOCs) suffer from chromatic bleeding, low GPU fill rates, and backlight inefficiency, the aesthetic discards computationally heavy backdrop filters and multi-stop gradients in favor of deep dusty rose-infused neutral field planes, crisp high-contrast silhouettes, and distinct high-luminance feedback cues. The emotional response is one of rock-solid operational dependability, operational precision, and effortless situational awareness across a showroom, retail floor, or control room.

## Colors

The palette balances energy conservation and anti-burn-in mitigation on sustained television displays with strict WCAG AAA luminance contrast for distance scanning.

- **Primary (`#966A84`)**: Dusty Mauve. Reserved for active focus boundaries, D-pad navigation targets, primary confirmations, and selection indicators. Its balanced optical emission cuts cleanly through dark panel fields.
- **Secondary (`#8A6F7E`)**: Muted Rose-Slate. Acts as a supportive anchoring shade for selected non-focused states, category tags, and secondary action surfaces.
- **Tertiary / Live Status (`#866E98`)**: Soft Lavender-Violet. Signals online kiosk endpoints, active playlist syncs, and healthy streaming nodes.
- **Warning / Alert (`#F59E0B`)**: Radiant Amber. Denotes degraded playback states, memory buffer pressure, offline cache status, or upcoming schedule expirations.
- **Neutrals & Surfaces**: Built from deeply saturated tonal-spot neutral surfaces rather than flat carbon blacks to preserve panel contrast without inducing pixel clipping.
  - Canvas Base: `#7E7578`
  - Elevated Container / Grid Base: `#7E7578`
  - Interactive Inactive Tiles & Borders: `#8A6F7E`
  - High Contrast Typography Primary: `#F8FAFC`
  - Muted Metadata / Sub-labels: `#BDC8D1`

## Typography

Typography is scaled and weighted strictly for the 10-foot user experience at native 1080p resolution (1920x1080 canvas). Fine line weights below 400 and font sizes below 15px are strictly forbidden to counter interlacing artifacts, sub-pixel rasterization quirks of older displays, and viewer distance.

- **Headline Hierarchy (Work Sans)**: Employs a robust, wide neo-grotesque posture with uniform open counters. This prevents character collapse at long viewing distances and maintains legibility even under high-glare retail lighting.
- **Body & Numerical Readouts (Inter)**: Delivers tall x-heights, distinct aperture definitions, and disambiguated glyphs (e.g., distinguishing `1`, `l`, and `I`). This is critical for reading playback timers, schedule timecodes, and stream bitrates.
- **Remote Legends (`label-key-legend`)**: Set in semi-bold uppercase with positive letter spacing (`0.05em`) to ensure instant pairing between physical remote buttons and on-screen hardware affordances.

## Layout & Spacing

The layout is built for fixed 1080p landscape (1920x1080) presentation. It avoids unpredictable content reflows in favor of a deterministic, 12-column rail system combined with horizontal carousels.

- **Title Safe and Overscan Zone**: To prevent hardware edge clipping common to older consumer televisions and commercial panels, all critical interactive controls, navigation trees, and status overlays must respect the `margin-overscan` padding (64px / 4rem) from physical screen borders.
- **Directional Grid Traversal**: Focusable elements are aligned to strict 2D coordinate matrices. Elements arranged horizontally or vertically must have uniform gutters (`gutter-tv`: 32px / 2rem) to ensure that directional D-pad key events (DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT) navigate to predictable, orthogonal neighboring targets.
- **Persistent Remote Legend Bar**: A fixed 48px baseline bar sits at the bottom edge within the safe zone, organizing physical D-pad commands (OK to Select, BACK to Step Out, MENU for Settings, D-PAD to Pan).

## Elevation & Depth

To maximize performance on resource-constrained Android 6 chipsets, the design system avoids real-time gaussian box-shadow rendering and alpha blurs. Depth is conveyed strictly via **Tonal Layering**, **High-Luminance Keyline Borders**, and **Physical Transform Scaling**.

1. **Base Plane (Level 0)**: Background root `#7E7578`. Non-interactive backdrop that anchors media players and dashboard layouts.
2. **Surface Containers (Level 1)**: Inactive cards, media schedule rows, and playlist lanes sit at `#7E7578` with a 1px solid stroke of `#8A6F7E`.
3. **Selected / Resting State (Level 2)**: Elements active in the background (e.g., currently playing track or active playlist) leverage `#8A6F7E` surfaces with a 1.5px `#8A6F7E` border.
4. **Focused / D-Pad Engaged State (Level 3)**:
   - Scale factor transform: `scale(1.05)` applied to the focused card to visually elevate it off the canvas.
   - Border treatment: Crisp, 3px solid `#966A84` ring with a 1px inset separation stroke.
   - Glow accent: A high-contrast, pre-baked 8px solid outer outline (`rgba(150, 106, 132, 0.45)`) that gives instant spatial prominence without burdening legacy GPU compositors.

## Shapes

The interface implements a **Pill-shaped / Soft Rounded** geometric standard.

- Standard UI cards, interactive tiles, input fields, and dialog boxes use a generous corner radius (`1rem` to `2rem`).
- Large modal sheets, media preview windows, and focus indicator rings use highly rounded radii (`2rem`+).
- Hardware legend indicators and small status badges utilize pill-shaped curves to preserve smooth visual rhythm across physical display panels.

## Components

### 1. Interactive Focusable Cards (Playlist & Media Items)
- **Resting**: Tonal fill, 1px border, highly rounded pill/corner radius. Thumbnail images use a 16:9 ratio with duration timestamps anchored in the bottom right corner.
- **Focused (D-pad targeting)**: Transforms to `scale(1.05)`. Outline becomes a 3px solid `#966A84` dusty mauve ring accompanied by an ambient halo. Card title shifts to high-contrast font weight 700.
- **Active / Playing**: Displays a persistent 3px left border accent in `#866E98` along with an animated playback indicator.

### 2. Remote Action Buttons
- **Default State**: Surface fill, crisp text (`Work Sans`, 18px, weight 600), interior padding `12px 24px`.
- **Focused State**: Solid `#966A84` background fill, text color changes for maximum contrast, scale `1.04`, with an outer focus border.
- **Legend Badges**: Embedded inline pill icons representing TV remote keys (`[OK]`, `[BACK]`, `[MENU]`) rendered with a tonal background and clear labels.

### 3. Hardware Remote Control Legend Bar
- Docked permanently inside the bottom safe zone.
- Horizontal row displaying contextual navigation help:
  - Mauve badge for primary action: `[OK] Play / Select`
  - Neutral badge for navigation: `[◄ ▲ ▼ ►] Move Focus`
  - Amber badge for utility: `[MENU] Kiosk Options`
  - Slate badge for exit: `[BACK] Return to Dashboard`

### 4. Status Badges & Sync Chips
- **Live / Healthy**: Background alpha tint, text `#866E98`, 1px stroke `#866E98`, pre-pended by an 8px solid circle.
- **Degraded / Syncing**: Background alpha tint, text `#F59E0B`, 1px stroke `#F59E0B`.
- **Offline / Stalled**: Background alpha tint, text `#EF4444`, 1px stroke `#EF4444`.

### 5. Dialogs and Override Overlays
- Modal dialogs sit centered on a semi-opaque scrim.
- Outlined by a prominent 2px solid `#966A84` border.
- All actions inside modals default D-pad focus to the non-destructive action, requiring explicit directional input to trigger destructive kiosk restarts or cache purges.