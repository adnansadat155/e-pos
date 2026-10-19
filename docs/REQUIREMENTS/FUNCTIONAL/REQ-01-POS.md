# Functional Requirements: Dinevique POS (Master Terminal)

**Document ID:** `REQ-01-POS`  
**Parent:** [SRS_INDEX.md](../SRS_INDEX.md)  
**Source Ref:** [SYSTEM_PRD.md](../../SOURCES/SYSTEM_PRD.md) §5.1, [SYSTEM_SRS.md](../../SOURCES/SYSTEM_SRS.md) §4.1  
**Module:** Dinevique POS (Native Android Master Terminal)  
**Icon:** `receipt` | **Accent:** Brand Amber (`#D4820A`)  
**Status:** DEFINED  

---

## Table of Contents
- [1. Module Overview](#1-module-overview)
- [2. Detailed Requirements](#2-detailed-requirements)
  - [FR-POS-001: Embedded Ktor Orchestrator & mDNS Discovery](#fr-pos-001-embedded-ktor-orchestrator--mdns-discovery)
  - [FR-POS-002: Fast Team PIN Switching & RBAC](#fr-pos-002-fast-team-pin-switching--rbac)
  - [FR-POS-003: Table Floor Plan, Seat Ordering & Coursing Engine](#fr-pos-003-table-floor-plan-seat-ordering--coursing-engine)
  - [FR-POS-004: Split Checks & Multi-Tender Settlement](#fr-pos-004-split-checks--multi-tender-settlement)
  - [FR-POS-005: Direct Hardware Peripheral Control](#fr-pos-005-direct-hardware-peripheral-control)
  - [FR-POS-006: Store-and-Forward Offline Payment Queue](#fr-pos-006-store-and-forward-offline-payment-queue)
- [3. Cross-Reference & Traceability](#3-cross-reference--traceability)

---

## 1. Module Overview
Dinevique POS serves as the in-store orchestrator and single source of edge truth. Implemented in native Kotlin and Jetpack Compose, it runs an embedded Ktor HTTP/WebSocket server in a persistent Android Foreground Service, manages the local Android Room SQLite database, directly drives hardware peripherals via `UsbManager`, and maintains the outward encrypted WSS sync tunnel to Dinevique Cloud.

---

## 2. Detailed Requirements

### FR-POS-001: Embedded Ktor Orchestrator & mDNS Discovery
- **Priority:** P0 (Must Have)
- **Source:** PRD POS-1 | SRS §4.1
- **User Story:** [US-POS-001](../USER_STORIES.md#story-us-pos-001-automatic-master-lan-discovery--ktor-service-initialization)
- **Description:** The Master Terminal shall host an embedded Ktor HTTP and WebSocket server running within an Android Foreground Service. It shall broadcast an mDNS service registration `_dinevique-pos._tcp.local.` advertising its local IPv4 address and port (default: 8080).
- **Inputs:** Network state broadcast, device boot completed intent.
- **Outputs:** Active Ktor listening socket, mDNS service record.
- **Verification Criteria:**
  - Ktor service starts automatically within 2 seconds of app launch.
  - Auxiliary Android clients discover the Master service within 2000ms without manual IP entry.
- **Fulfillment:** Spec: `SPEC-POS-MASTER.md` | Contract: `EVENT-SYS-DISCOVERY.md` | Test: `TC-POS-001`

### FR-POS-002: Fast Team PIN Switching & RBAC
- **Priority:** P0 (Must Have)
- **Source:** PRD POS-2 | SRS §4.1
- **User Story:** [US-POS-002](../USER_STORIES.md#story-us-pos-002-sub-300ms-team-pin-authentication--rapid-lock)
- **Description:** The terminal shall authenticate team members via a 4–6 digit numeric PIN pad against locally hashed credentials in Room SQLite. The system shall enforce role-based access control (*Server*, *Cashier*, *Cook*, *Manager*) and automatically lock after 60 seconds of inactivity.
- **Inputs:** Numeric keypad touch events (4–6 digits).
- **Outputs:** Authenticated user session state token, role permissions object.
- **Verification Criteria:**
  - PIN verification completes in < 50ms locally.
  - Active check and floor plan render in Jetpack Compose in < 300ms.
- **Fulfillment:** Spec: `SPEC-POS-MASTER.md` | Contract: `SCHEMA-USER-SESSION.md` | Test: `TC-POS-002`

### FR-POS-003: Table Floor Plan, Seat Ordering & Coursing Engine
- **Priority:** P0 (Must Have)
- **Source:** PRD POS-3 | SRS §4.1
- **User Story:** [US-POS-003](../USER_STORIES.md#story-us-pos-003-table-floor-plan-seat-assignment--course-holds)
- **Description:** The system shall display a customizable table layout indicating occupancy, guest headcount, and open duration. Orders shall support seat-level item assignments and course tags (`Hold` vs. `Fire`). Fired courses dispatch `TICKET_RECEIVED` events to the KDB, while held courses remain queued until explicitly fired.
- **Inputs:** Table selection, seat assignments, item touch, course toggles.
- **Outputs:** Persisted `local_orders` record in Room SQLite, `TICKET_RECEIVED` WebSocket broadcast.
- **Verification Criteria:**
  - Only items marked `Fire` are dispatched to KDB.
  - Firing a held course emits an event within 200ms.
- **Fulfillment:** Spec: `SPEC-POS-MASTER.md` | Contract: `EVENT-ORDER-CREATE.md` | Test: `TC-POS-003`

### FR-POS-004: Split Checks & Multi-Tender Settlement
- **Priority:** P0 (Must Have)
- **Source:** PRD POS-4 | SRS §4.1
- **User Story:** [US-POS-004](../USER_STORIES.md#story-us-pos-004-split-checks-by-seat-and-even-n-ways-with-penny-perfect-rounding)
- **Description:** The system shall support splitting checks by seat number, moving individual items between sub-checks, splitting checks evenly across N parties (up to 20), and accepting multiple payment tenders per check with penny-perfect sales tax rounding.
- **Inputs:** Split command, seat indices, N party count, tender amounts.
- **Outputs:** Child check records with balanced tax allocations.
- **Verification Criteria:**
  - Sum of split checks exactly equals original total in cents.
  - Discrepancy is guaranteed 0 cents.
- **Fulfillment:** Spec: `SPEC-POS-MASTER.md` | Contract: `SCHEMA-ORDER.md` | Test: `TC-POS-004`

### FR-POS-005: Direct Hardware Peripheral Control
- **Priority:** P0 (Must Have)
- **Source:** PRD POS-5 | SRS §4.1
- **User Story:** [US-POS-005](../USER_STORIES.md#story-us-pos-005-direct-hardware-control-of-printers-and-cash-drawers)
- **Description:** The system shall communicate directly with receipt printers, kitchen impact printers, and cash drawers using Android `UsbManager`, Epson ePOS SDK, and StarPRNT SDK. Cash payments shall trigger an immediate hardware drawer kick pulse.
- **Inputs:** Transaction commit event, tender type.
- **Outputs:** Raw ESC/POS byte sequence over USB Host / Ethernet socket.
- **Verification Criteria:**
  - Cash drawer kicks in < 100ms.
  - Receipt begins printing within 150ms.
- **Fulfillment:** Spec: `SPEC-POS-HARDWARE.md` | Contract: `CONTRACT-HARDWARE-IO.md` | Test: `TC-POS-005`

### FR-POS-006: Store-and-Forward Offline Payment Queue
- **Priority:** P0 (Must Have)
- **Source:** PRD POS-6 | SRS §4.1
- **User Story:** [US-POS-006](../USER_STORIES.md#story-us-pos-006-store-and-forward-offline-payment-authorizations)
- **Description:** During WAN outages, the terminal shall encrypt card data with AES-256 GCM using keys in the Android Hardware Keystore, append the transaction to `offline_payment_queue`, close the order locally, and enforce configurable single-transaction ($150) and cumulative ($2,500) risk limits.
- **Inputs:** Offline card swipe/dip/tap payload, order total.
- **Outputs:** Encrypted payment entity in Room SQLite, closed order record.
- **Verification Criteria:**
  - Card PAN and CVV never appear in plaintext anywhere in memory or storage.
  - Attempted offline transactions exceeding limits are rejected with explanatory UI.
- **Fulfillment:** Spec: `SPEC-POS-MASTER.md` | Contract: `SCHEMA-OFFLINE-PAYMENT.md` | Test: `TC-POS-006`

---

## 3. Cross-Reference & Traceability

| Requirement ID | User Story | Contract / Schema | Architecture | Verification Test |
| :--- | :--- | :--- | :--- | :--- |
| `FR-POS-001` | `US-POS-001` | `EVENT-SYS-DISCOVERY` | `SPEC-POS-MASTER.md` | `TC-POS-001` |
| `FR-POS-002` | `US-POS-002` | `SCHEMA-USER-SESSION` | `SPEC-POS-MASTER.md` | `TC-POS-002` |
| `FR-POS-003` | `US-POS-003` | `EVENT-ORDER-CREATE` | `SPEC-POS-MASTER.md` | `TC-POS-003` |
| `FR-POS-004` | `US-POS-004` | `SCHEMA-ORDER` | `SPEC-POS-MASTER.md` | `TC-POS-004` |
| `FR-POS-005` | `US-POS-005` | `CONTRACT-HARDWARE-IO`| `SPEC-POS-HARDWARE.md`| `TC-POS-005` |
| `FR-POS-006` | `US-POS-006` | `SCHEMA-OFFLINE-PAYMENT`| `SPEC-POS-MASTER.md` | `TC-POS-006` |

---
_Document: REQ-01-POS.md | Node: REQUIREMENTS/FUNCTIONAL | Parent: [SRS_INDEX.md](../SRS_INDEX.md)_
