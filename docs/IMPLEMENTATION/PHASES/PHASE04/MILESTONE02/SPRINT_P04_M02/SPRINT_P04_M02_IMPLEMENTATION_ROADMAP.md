# Sprint Implementation Roadmap: SPRINT_P04_M02

**Sprint ID:** `SPRINT_P04_M02`  
**Milestone:** Phase 04 — Milestone 02: Handheld Tap-to-Pay & Digital Receipts  
**Parent Milestone Index:** [MILESTONE02_INDEX.md](../MILESTONE02_INDEX.md)  
**Status:** 🟡 **ACTIVE**  
**Estimated Velocity:** 4 Tasks | 100% Passing Target  

---

## 1. Sprint Objective

Empower waitstaff with rapid tableside payment collection on mobile hardware (Sunmi V2 / Android Handhelds) while giving dining guests seamless digital receipts:
1. Deliver `TapToPayEngine.kt` to handle contactless EMV / NFC kernel card reads in $< 800\text{ms}$.
2. Deliver `HandheldPaymentDialog.kt` in Jetpack Compose featuring contactless wave animation, tip selection chips, and instant approval confirmation.
3. Deliver the Cloud Gateway Digital Receipt Controller (`DigitalReceiptController.ts`) and responsive customer web receipt portal (`/receipt/[receiptUuid]`).
4. Validate all flows via an automated test suite.

---

## 2. Work Breakdown Structure (WBS)

### Task T01: Contactless EMV / NFC Kernel Engine
- **Location:** `apps/android/app-pos/src/main/java/com/dinevique/pos/payments/TapToPayEngine.kt`
- **Requirements:**
  - Sunmi Pay / Android Contactless Reader hardware abstraction.
  - AID routing: Visa Contactless (`A0000000031010`), Mastercard Contactless (`A0000000041010`), Amex ExpressPay (`A00000002501`).
  - Cardholder Verification Method (CVM) logic: No PIN required under $100 ceiling.
  - PAN masking: Strictly truncates card number to last 4 digits (`•••• 4242`).

### Task T02: Handheld Jetpack Compose Payment Flow UI
- **Location:** `apps/android/app-pos/src/main/java/com/dinevique/pos/ui/HandheldPaymentDialog.kt`
- **Requirements:**
  - Tableside tip calculator (15%, 18%, 20%, Custom, No Tip) updating total dynamically.
  - Visual Contactless EMV Wave animation.
  - 4 lifecycle states: `AWAITING_CARD`, `READING_NFC`, `AUTHORIZING`, `APPROVED`.
  - Brand compliance: Brand Onyx (`#0D0D0D`), Brand Slate (`#1E2A38`), Brand Amber (`#D4820A`), Brand Ink (`#1A1A1A`), Zero exclamation marks.

### Task T03: Omnichannel Digital Receipt Dispatcher & Web Viewer
- **Location:**
  - Gateway: `apps/cloud/gateway/src/controllers/DigitalReceiptController.ts`, `server.ts`
  - Web: `apps/cloud/web/src/app/receipt/[receiptUuid]/page.tsx`
- **Requirements:**
  - `POST /api/v1/receipts/send`: Dispatches digital receipt via SMS/Email or returns viewable link.
  - `GET /api/v1/receipts/:receiptUuid`: Returns receipt data.
  - Next.js Web Receipt: Itemized breakdown, card tender info, download/print buttons, WCAG AA compliant buttons (`#D4820A` paired with `#1A1A1A`).

### Task T04: Automated Verification Test Suite
- **Location:** `apps/cloud/gateway/test/handheld-tap-and-receipts.test.mjs`
- **Requirements:**
  - Validate EMV Contactless AID selection and PAN masking.
  - Validate tip arithmetic and total calculation.
  - Validate digital receipt creation and retrieval.

---
_Sprint Roadmap: SPRINT_P04_M02 | Phase 04: Resilience & Payments | Status: ACTIVE_
