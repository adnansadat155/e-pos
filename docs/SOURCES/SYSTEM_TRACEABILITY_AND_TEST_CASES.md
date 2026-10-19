# System Traceability and Test Cases: Dinevique Platform

**Version:** 1.0.0  
**Parent Document:** [SYSTEM_PRD.md](SYSTEM_PRD.md)  
**Cross-References:** [SYSTEM_SRS.md](SYSTEM_SRS.md), [SYSTEM_USER_STORIES.md](SYSTEM_USER_STORIES.md), [REQUIREMENTS_TRACEABILITY_MATRIX.md](../REQUIREMENTS/REQUIREMENTS_TRACEABILITY_MATRIX.md)  
**Date:** 2026-10-19  
**Status:** Approved Baseline  

---

## 1. Traceability Overview

This document establishes the bidirectional verification matrix connecting high-level product requirements, functional specifications, user stories, software modules, and automated verification suites across all five Dinevique modules.

```
PRD Feature ID
      ↓
SRS Requirement ID (FR-*)
      ↓
User Story ID (US-*)
      ↓
Architecture & Spec Component (SPEC-*)
      ↓
Data Contract & Schema (CONTRACT-* / SCHEMA-*)
      ↓
Automated Test Case (TC-*)
```

---

## 2. Complete Traceability Matrix

| PRD Ref | SRS Functional Req | User Story ID | Architecture Component | Contract / Schema | Test Case ID | Target Phase & Sprint |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **POS-1** | `FR-POS-001` (Ktor Orchestrator) | `US-POS-001` | `SPEC-POS-MASTER.md` | `EVENT-SYS-DISCOVERY` | `TC-POS-001` | Phase 01 / Sprint M01 |
| **POS-2** | `FR-POS-002` (PIN Switch & RBAC) | `US-POS-002` | `SPEC-POS-MASTER.md` | `SCHEMA-USER-SESSION` | `TC-POS-002` | Phase 01 / Sprint M01 |
| **POS-3** | `FR-POS-003` (Floor Map & Coursing)| `US-POS-003` | `SPEC-POS-MASTER.md` | `EVENT-ORDER-CREATE` | `TC-POS-003` | Phase 02 / Sprint M01 |
| **POS-4** | `FR-POS-004` (Split Checks/Tenders)| `US-POS-004` | `SPEC-POS-MASTER.md` | `SCHEMA-ORDER` | `TC-POS-004` | Phase 02 / Sprint M02 |
| **POS-5** | `FR-POS-005` (Hardware I/O Control)| `US-POS-005` | `SPEC-POS-HARDWARE.md`| `CONTRACT-HARDWARE-IO`| `TC-POS-005` | Phase 02 / Sprint M03 |
| **POS-6** | `FR-POS-006` (Store & Forward Pay) | `US-POS-006` | `SPEC-POS-MASTER.md` | `SCHEMA-OFFLINE-PAYMENT`|`TC-POS-006` | Phase 04 / Sprint M01 |
| **KDB-1** | `FR-KDB-001` (Real-Time Ticket Stream)|`US-KDB-001` | `SPEC-KITCHEN-KDB.md` | `EVENT-TICKET-RECEIVED` | `TC-KDB-001` | Phase 01 / Sprint M02 |
| **KDB-2** | `FR-KDB-002` (Ticket Bump Workflow)| `US-KDB-002` | `SPEC-KITCHEN-KDB.md` | `EVENT-TICKET-BUMPED` | `TC-KDB-002` | Phase 01 / Sprint M02 |
| **KDB-3** | `FR-KDB-003` (Station Filter & Recall)|`US-KDB-003` | `SPEC-KITCHEN-KDB.md` | `EVENT-TICKET-RECALLED` | `TC-KDB-003` | Phase 02 / Sprint M01 |
| **KDB-4** | `FR-KDB-004` (Pass 86ing Broadcast) | `US-KDB-004` | `SPEC-KITCHEN-KDB.md` | `EVENT-ITEM-86` | `TC-KDB-004` | Phase 02 / Sprint M02 |
| **HND-1** | `FR-HND-001` (Ergonomic 3-Tap UI) | `US-HND-001` | `SPEC-HANDHELD-MOBILE.md`| `EVENT-ORDER-CREATE` | `TC-HND-001` | Phase 02 / Sprint M01 |
| **HND-2** | `FR-HND-002` (LAN Proxy Sync) | `US-HND-002` | `SPEC-HANDHELD-MOBILE.md`| `CONTRACT-LAN-WEBSOCKET`|`TC-HND-002` | Phase 02 / Sprint M01 |
| **HND-3** | `FR-HND-003` (Tableside Tap-to-Pay) | `US-HND-003` | `SPEC-HANDHELD-MOBILE.md`| `SCHEMA-PAYMENT-RECORD` | `TC-HND-003` | Phase 04 / Sprint M02 |
| **ONL-1** | `FR-ONL-001` (SSR Digital Menu) | `US-ONL-001` | `SPEC-ONLINE-STOREFRONT.md`| `SCHEMA-MENU-CANONICAL`| `TC-ONL-001` | Phase 03 / Sprint M01 |
| **ONL-2** | `FR-ONL-002` (Direct Edge Push) | `US-ONL-002` | `SPEC-ONLINE-STOREFRONT.md`| `EVENT-ORDER-CLOUD-INGEST`|`TC-ONL-002` | Phase 03 / Sprint M01 |
| **ONL-3** | `FR-ONL-003` (Live Status Tracker) | `US-ONL-003` | `SPEC-ONLINE-STOREFRONT.md`| `EVENT-ORDER-STATUS-UPDATE`|`TC-ONL-003`| Phase 03 / Sprint M02 |
| **CLD-1** | `FR-CLD-001` (Hierarchical Menu Studio)|`US-CLD-001`| `SPEC-CLOUD-ADMIN.md` | `EVENT-MENU-SYNC` | `TC-CLD-001` | Phase 03 / Sprint M01 |
| **CLD-2** | `FR-CLD-002` (Idempotent Ingestion) | `US-CLD-002` | `SPEC-CLOUD-GATEWAY.md` | `CONTRACT-IDEMPOTENCY` | `TC-CLD-002` | Phase 01 / Sprint M03 |
| **CLD-3** | `FR-CLD-003` (Analytics Dashboard) | `US-CLD-003` | `SPEC-CLOUD-ADMIN.md` | `API-ANALYTICS-SUMMARY` | `TC-CLD-003` | Phase 03 / Sprint M02 |

---

## 3. Detailed Automated Test Cases

### TC-POS-001: Ktor Discovery & Service Resiliency
- **Scope:** Integration Test (Android Foreground Service)
- **Method:** Automated Network Simulation
- **Steps:**
  1. Boot Android mock service with `KtorEngine`.
  2. Query `_dinevique-pos._tcp.local.` via NSD client.
  3. Verify resolved address and port `8080`.
  4. Disconnect Wi-Fi interface for 5 seconds; re-enable.
- **Pass Criteria:** Discovery resolves in < 2000ms; auto-rebinds after interface flap in < 3000ms.

### TC-POS-002: PIN Validation & Compose Render Latency
- **Scope:** UI Benchmark (Jetpack Compose Macrobenchmark)
- **Method:** Instrumented Test
- **Steps:**
  1. Input 4-digit PIN string `4821`.
  2. Time Room hash lookup and state emission.
  3. Measure time to first frame of FloorPlanScreen.
- **Pass Criteria:** Total elapsed duration from 4th key tap to FloorPlan render is <= 300ms.

### TC-POS-004: Split Check Tax Balancing
- **Scope:** Unit Test (Financial Calculations)
- **Method:** Parameterized Test
- **Steps:**
  1. Create order total of $99.95 with 8.875% tax.
  2. Split 3 ways evenly.
  3. Sum itemized sub-totals and calculated taxes.
- **Pass Criteria:** Sum of split checks matches original cent total exactly; discrepancy is 0 cents.

### TC-POS-006: Offline Payment Encryption & Storage
- **Scope:** Security & Integration Test
- **Method:** Automated Mock Hardware Sled
- **Steps:**
  1. Disconnect WAN mock gateway.
  2. Emit card swipe event ($48.00).
  3. Query `offline_payment_queue` Room table.
  4. Inspect stored payload.
- **Pass Criteria:** Plaintext PAN/CVV is absent; payload verifies AES-256 GCM authentication tag; check status is `PAID (OFFLINE)`.

### TC-KDB-001: LAN WebSocket Ticket Transit Latency
- **Scope:** End-to-End Performance Test
- **Method:** Distributed Timing
- **Steps:**
  1. Handheld emits `ORDER_CREATE` at timestamp $T_0$.
  2. Master POS writes to Room and broadcasts `TICKET_RECEIVED`.
  3. KDB records ticket arrival timestamp $T_1$.
- **Pass Criteria:** $(T_1 - T_0) \le 400\text{ms}$ in 99 of 100 test iterations.

### TC-KDB-002: Bump Micro-Interaction Visual Validation
- **Scope:** UI / Accessibility Test
- **Method:** Screenshot & Composition Assert
- **Steps:**
  1. Inject active ticket on KDB.
  2. Perform click on `BUMP ->`.
  3. Sample card frame color at $t = 50\text{ms}$.
- **Pass Criteria:** Frame color equals Brand Amber (`#D4820A`); ticket dismisses and emits `TICKET_BUMPED`.

### TC-CLD-002: Idempotent Cloud Batch Replay
- **Scope:** Backend Stress & Concurrency Test
- **Method:** Gateway Replay Injection
- **Steps:**
  1. Generate 50 unique order events with client UUIDs.
  2. Transmit batch to Node.js `/api/v1/sync/orders` endpoint.
  3. Transmit identical batch immediately with random packet interleaving.
- **Pass Criteria:** MongoDB collection count is exactly 50; zero duplicate revenue or order records.
