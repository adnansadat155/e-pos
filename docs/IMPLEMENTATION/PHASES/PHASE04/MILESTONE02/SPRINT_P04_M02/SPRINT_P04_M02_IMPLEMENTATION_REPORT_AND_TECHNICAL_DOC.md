# Sprint Implementation Report & Technical Specification: SPRINT_P04_M02

**Sprint ID:** `SPRINT_P04_M02`  
**Milestone:** Phase 04 — Milestone 02: Handheld Tap-to-Pay & Digital Receipts  
**Parent Milestone Index:** [MILESTONE02_INDEX.md](../MILESTONE02_INDEX.md)  
**Parent Roadmap:** [SPRINT_P04_M02_IMPLEMENTATION_ROADMAP.md](SPRINT_P04_M02_IMPLEMENTATION_ROADMAP.md)  
**Status:** 🟡 **DOCUMENTATION COMMITTED (PRE-CODE)**  
**Version:** 1.0.0  

---

## 1. Contactless EMV Level 2 Kernel Architecture

The handheld payment subsystem integrates directly with hardware contactless interfaces (e.g., Sunmi V2 NFC reader / ISO 14443 Type A/B transceiver):

### 1.1 Application Identifier (AID) Selection
Upon card detection within the RF field ($< 4\text{cm}$), the kernel queries the Proximity Integrated Circuit Card (PICC) using ISO 7816-4 APDU commands:
- **Visa Contactless (payWave):** `A0000000031010`
- **Mastercard Contactless (PayPass):** `A0000000041010`
- **American Express (ExpressPay):** `A00000002501`

### 1.2 Cardholder Verification Method (CVM) Rules
- For amounts $\le \$100.00$ ($10,000$ cents): CVM evaluates to `NO_CVM_REQUIRED` (Quick Chip / Express Checkout).
- For amounts $> \$100.00$: CVM requires On-Device Cardholder Verification (ODCVM for Apple Pay/Google Pay) or Signature/PIN prompt.

### 1.3 Card Data Truncation & Tokenization
To uphold PCI-DSS standards:
- Full Primary Account Number (PAN) is never exposed to application logs or plain storage.
- Masked format: `•••• •••• •••• 4242` (preserves brand identification and Last 4 digits).

---

## 2. Gratuity Model & Calculation Rules

Tableside tip percentage calculation enforces penny-perfect mathematical rounding:
$$\text{TipCents} = \text{round}\Big( \text{SubtotalCents} \times \frac{\text{Percent}}{100} \Big)$$
$$\text{GrandTotalCents} = \text{SubtotalCents} + \text{TaxCents} + \text{TipCents}$$

Standard preset chips:
- `15%`: Standard service
- `18%`: Recommended default
- `20%`: Exceptional hospitality
- `Custom`: Numerical keypad input
- `No Tip`: 0 gratuity

---

## 3. Omnichannel Digital Receipt Dispatch Protocol

Digital receipts eliminate thermal paper waste while allowing dining guests to view, print, or download receipts on any device:
- **API Endpoint:** `POST /api/v1/receipts/send`
  - Input: `{ orderUuid, paymentUuid, recipientEmail?, recipientPhone?, deliveryMethod: 'SMS' | 'EMAIL' | 'QR_CODE' }`
  - Output: `{ receiptUuid, publicUrl, status: 'DISPATCHED' }`
- **Web Receipt Portal:** `GET /receipt/:receiptUuid`
  - Renders official Dinevique itemized bill with merchant branding, server name, table, itemized list, modifiers, and payment tender summary.

---

## 4. Dinevique Brand Tokens & Design System Enforcement

All Handheld and Web views strictly enforce:
- **Background:** Brand Onyx (`#0D0D0D`)
- **Card Surfaces:** Brand Slate (`#1E2A38`)
- **Interactive Accents:** Brand Amber (`#D4820A`)
- **Text on Amber:** Brand Ink (`#1A1A1A`) for WCAG AA compliance (6.4:1 contrast ratio)
- **Typography:** Inter
- **Copy Restrictions:** Zero exclamation marks across all dialogs, buttons, and notifications.

---
_Technical Specification: SPRINT_P04_M02 | Phase 04: Milestone 02 | Status: DOCUMENTATION COMMITTED (PRE-CODE)_
