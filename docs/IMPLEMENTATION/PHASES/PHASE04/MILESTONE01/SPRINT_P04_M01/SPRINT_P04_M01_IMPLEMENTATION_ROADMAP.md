# Sprint Implementation Roadmap: SPRINT_P04_M01

**Sprint ID:** `SPRINT_P04_M01`  
**Milestone:** Phase 04 — Milestone 01: Store-and-Forward Offline Payments  
**Parent Milestone Index:** [MILESTONE01_INDEX.md](../MILESTONE01_INDEX.md)  
**Status:** 🟡 **ACTIVE**  
**Estimated Velocity:** 4 Tasks | 100% Passing Target  

---

## 1. Sprint Objective

Deliver mission-critical offline card payment resilience allowing restaurants to operate seamlessly without an active WAN internet connection:
1. Implement hardware-backed AES-256 GCM encryption for sensitive card payment tokens before persisting to Room SQLite `offline_payment_queue`.
2. Enforce rigorous risk controls: single transaction floor limit of $150.00 (15,000 cents) and cumulative restaurant ceiling of $5,000.00 (500,000 cents).
3. Build the background WAN connectivity observer and auto-drain recovery loop that replays queued payments idempotently upon connection restore.
4. Build the Cloud Payment Gateway settlement endpoints in `@dinevique/gateway` supporting single and batch transaction reconciliation.
5. Author and run automated verification tests covering encryption, floor limits, replay latency, and zero duplicate charges.

---

## 2. Work Breakdown Structure (WBS)

### Task T01: Android AES-256 GCM Encryption & Queue Manager
- **Location:** `apps/android/app-pos/src/main/java/com/dinevique/pos/payments/OfflinePaymentManager.kt`
- **Requirements:**
  - AES-256 GCM cipher initialization with random 12-byte IV and 128-bit authentication tag.
  - Risk checks:
    - Single transaction: Reject with `EXCEEDS_SINGLE_LIMIT` if `amountCents > 15000`.
    - Cumulative queue: Reject with `EXCEEDS_CUMULATIVE_RISK_LIMIT` if `currentQueuedCents + amountCents > 500000`.
  - Record construction into `OfflinePaymentQueueEntity` and Room insertion via `OfflinePaymentDao`.

### Task T02: WAN Connectivity Observer & Auto-Drain Recovery Worker
- **Location:** `apps/android/app-pos/src/main/java/com/dinevique/pos/payments/PaymentDrainManager.kt`
- **Requirements:**
  - Network state listener (WAN Online vs Offline).
  - Chronological batch drain: reads queued items from `OfflinePaymentDao`, posts to Cloud Gateway `/api/v1/payments/batch-settle`.
  - Idempotent acknowledgment handling: marks items as `SETTLED` or `FAILED`.
  - Queue depth and cumulative risk telemetry reporting.

### Task T03: Cloud Payment Gateway Settlement Controller
- **Location:** `apps/cloud/gateway/src/controllers/PaymentController.ts`, `models/PaymentModel.ts`, `server.ts`
- **Requirements:**
  - `POST /api/v1/payments/process`: Standard online payment capture.
  - `POST /api/v1/payments/batch-settle`: Replays offline authorized transactions with deduplication ($set upsert on `paymentUuid`).
  - `GET /api/v1/payments/stats`: Returns count and volume of settled vs queued transactions.

### Task T04: Automated Verification Test Suite
- **Location:** `apps/cloud/gateway/test/offline-payment.test.mjs`
- **Requirements:**
  - Test AES-256 GCM encryption/decryption round-trip.
  - Test floor limits enforcement ($150 single, $5,000 cumulative).
  - Test batch settlement replay with zero duplication on repeated requests.

---

## 3. Strict Acceptance Criteria & SLAs

| Criteria | Target SLA / Standard | Verification Method |
| :--- | :--- | :--- |
| Offline Authorization Latency | $< 250\text{ms}$ (Local Room DB insert) | Benchmark test in Kotlin/Node runner |
| Encryption Standard | AES-256 GCM NoPadding | Unit test cipher check |
| Risk Limits Enforcement | $150.00 single / $5,000 cumulative | Edge condition boundary tests |
| Batch Settlement Idempotency | 0 duplicate charges on replay | Repeated batch settlement request |

---
_Sprint Roadmap: SPRINT_P04_M01 | Phase 04: Resilience & Payments | Status: ACTIVE_
