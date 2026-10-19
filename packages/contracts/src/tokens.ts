/**
 * Dinevique Brand Tokens & Color Guardrails (v1.0)
 * Source: docs/SOURCES/SYSTEM_BRAND_AND_STYLE_GUIDELINE.md
 */

export const DineviqueBrandTokens = {
  colors: {
    // Canvas & Surfaces
    brandOnyx: '#0D0D0D', // Primary dark canvas for KDB, dark POS, web hero
    brandSlate: '#1E2A38', // Secondary dark surface for KDB ticket frames, sidebars
    brandIvory: '#F7F5F1', // Light background for web menus and cloud portal
    brandLinen: '#E8E4DC', // 1px borders, dividers, subtle frames

    // Brand Primary Interactive Accent
    brandAmber: '#D4820A', // Primary CTA fill, active ticket borders, badge highlights

    // Text & Reading
    brandInk: '#1A1A1A', // Primary high-contrast text on light backgrounds and on Brand Amber fills
    brandWhite: '#FFFFFF', // High-contrast text on Brand Onyx and Brand Slate

    // Semantic Status Colors
    statusSuccess: '#2D7D46', // Success Green - NEW TICKET badge, sync confirmed
    statusAlert: '#C0392B', // Alert Red - WAN interruption, payment decline, order cancel
    statusInfo: '#2563EB', // Info Blue - Dinevique Kitchen module chip, active timers
    statusPending: '#B8860B', // Pending Ochre - Dinevique Handheld chip, queued items
  },
  typography: {
    primaryFont: 'Inter, system-ui, -apple-system, sans-serif',
    displayFont: 'Playfair Display, Georgia, serif',
  },
  accessibility: {
    minTouchTargetDp: 48,
    minKdbTouchTargetDp: 64,
    // Contrast Guardrail: Never use brandAmber text on brandWhite or brandIvory
    isValidAmberUsage: (isBackgroundFill: boolean, textColor: string) => {
      if (isBackgroundFill) {
        return textColor === '#1A1A1A'; // Must pair with Brand Ink
      }
      return false; // Prohibited as text on light canvas
    },
  },
} as const;

export type BrandColors = typeof DineviqueBrandTokens.colors;
