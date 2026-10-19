# Non-Functional Requirements: Accessibility & Brand Compliance

**Document ID:** `NFR-ACCESSIBILITY-BRAND`  
**Parent:** [SRS_INDEX.md](../SRS_INDEX.md)  
**Source Ref:** [SYSTEM_PRD.md](../../SOURCES/SYSTEM_PRD.md) §7, [SYSTEM_BRAND_AND_STYLE_GUIDELINE.md](../../SOURCES/SYSTEM_BRAND_AND_STYLE_GUIDELINE.md)  
**Category:** Design System, Accessibility & Voice  
**Status:** DEFINED  

---

## 1. Overview & Brand Identity
All Dinevique interfaces across Android and Next.js must strictly conform to the **Dinevique Strategic Brand Guidelines v1.0**. Every screen must embody *The Art of Smarter Dining* through calm visual hierarchy, WCAG AA legibility, ergonomic touch targets, and a measured, active voice.

---

## 2. Accessibility & Visual Standards

| Requirement ID | Standard | Specification | Verification Rule |
| :--- | :--- | :--- | :--- |
| **NFR-A11Y-001** | **Contrast Ratios (WCAG 2.1 AA)** | Normal text >= 4.5:1; Large text & UI icons >= 3.0:1 | **CRITICAL:** Brand Amber (`#D4820A`) must never be used as text on white/ivory (~3.0:1 FAIL). Must always pair `#D4820A` fills with Brand Ink (`#1A1A1A`) text (~6.4:1 PASS). |
| **NFR-A11Y-002** | **Touch Target Dimensions** | Minimum 48 x 48dp on all mobile and POS screens; minimum 64 x 64dp on KDB displays | No interactive element may overlap or fall below 48dp on either axis. |
| **NFR-A11Y-003** | **Motion & Reduced Motion** | Screen transitions: 250–300ms cubic-bezier(0.4, 0, 0.2, 1); micro-interactions: 100–120ms | Must honor `prefers-reduced-motion` media queries and Android animator duration scale. |
| **NFR-A11Y-004** | **Typography Standards** | **Inter** (300–800) for all product UI; **Playfair Display** strictly for marketing headlines | Body text minimum 14px (12px absolute minimum for small metadata). Left-aligned; no justified text. |
| **NFR-A11Y-005** | **Iconography System** | **Lucide** outlined icons with 2px stroke and 2–3px rounded corners | Emoji are strictly forbidden across all product interfaces and official documents. |

---

## 3. Brand Tone & Copy Guardrails

| Rule ID | Tone Principle | Approved Pattern | Prohibited Pattern |
| :--- | :--- | :--- | :--- |
| **NFR-TONE-001** | **Active Verbs on Actions** | `Print Ticket`, `Send Order`, `Bump ->` | Vague or passive labels like `OK`, `Proceed`, `Submit` |
| **NFR-TONE-002** | **No Exclamation Marks** | `Order sent to kitchen.` | `Order sent successfully!` (Exclamation marks forbidden) |
| **NFR-TONE-003** | **Transparent Error Guidance** | State what happened, why, and exact recovery step | `An error occurred. Please try again.` |

---
_Document: NFR-ACCESSIBILITY-BRAND.md | Node: REQUIREMENTS/NON_FUNCTIONAL | Parent: [SRS_INDEX.md](../SRS_INDEX.md)_
