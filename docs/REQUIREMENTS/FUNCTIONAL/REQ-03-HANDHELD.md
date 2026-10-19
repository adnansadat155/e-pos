# Functional Requirements: Dinevique Handheld (Mobile Waitstaff POS)

**Document ID:** `REQ-03-HANDHELD`  
**Parent:** [SRS_INDEX.md](../SRS_INDEX.md)  
**Source Ref:** [SYSTEM_PRD.md](../../SOURCES/SYSTEM_PRD.md) §5.3, [SYSTEM_SRS.md](../../SOURCES/SYSTEM_SRS.md) §4.3  
**Module:** Dinevique Handheld (Native Android Mobile POS)  
**Icon:** `smartphone` | **Accent:** Pending Ochre (`#B8860B`)  
**Status:** DEFINED  

---

## Table of Contents
- [1. Module Overview](#1-module-overview)
- [2. Detailed Requirements](#2-detailed-requirements)
  - [FR-HND-001: Ergonomic 5–7" Mobile UI & 3-Tap Order Entry](#fr-hnd-001-ergonomic-57-mobile-ui--3-tap-order-entry)
  - [FR-HND-002: Delegated Master Terminal Proxy Synchronization](#fr-hnd-002-delegated-master-terminal-proxy-synchronization)
  - [FR-HND-003: Tableside Tap-to-Pay & Digital Receipt Dispatch](#fr-hnd-003-tableside-tap-to-pay--digital-receipt-dispatch)
- [3. Cross-Reference & Traceability](#3-cross-reference--traceability)

---

## 1. Module Overview
Dinevique Handheld delivers a thumb-friendly mobile ordering and payment experience running on 5-to-7-inch handheld devices (such as the Sunmi V2/V2 Pro). Designed with zero local database or peripheral dependencies, it delegates all reads, transactions, and hardware actions strictly to the Master Terminal over local LAN WebSockets.

---

## 2. Detailed Requirements

### FR-HND-001: Ergonomic 5–7" Mobile UI & 3-Tap Order Entry
- **Priority:** P0 (Must Have)
- **Source:** PRD HND-1 | SRS §4.3
- **User Story:** [US-HND-001](../USER_STORIES.md#story-us-hnd-001-3-tap-mobile-order-entry-on-sunmi-v2-hardware)
- **Description:** The client shall provide an ergonomic touch interface tailored for one-handed operation. Selecting an item, satisfying mandatory modifiers via a bottom sheet, and firing the order to the Master Terminal must be achievable in 3 taps or fewer.
- **Inputs:** Touch tap gestures on category chips, item cards, modifier options, and bottom bar CTA.
- **Outputs:** In-flight order payload sent via local WebSocket.
- **Verification Criteria:**
  - Order entry flow from item list to dispatch takes <= 3 taps for items with single required modifier groups.
  - UI touch feedback renders in < 100ms.
- **Fulfillment:** Spec: `SPEC-HANDHELD-MOBILE.md` | Contract: `EVENT-ORDER-CREATE.md` | Test: `TC-HND-001`

### FR-HND-002: Delegated Master Terminal Proxy Synchronization
- **Priority:** P0 (Must Have)
- **Source:** PRD HND-2 | SRS §4.3
- **User Story:** [US-HND-002](../USER_STORIES.md#story-us-hnd-002-delegated-lan-master-terminal-synchronization)
- **Description:** Handheld clients shall route 100% of network traffic through the Master Terminal's Ktor server over local Wi-Fi. Handhelds shall maintain zero direct connections to the cloud database or physical network printers, ensuring full operational capability when external WAN is down.
- **Inputs:** Local Wi-Fi connection, Master Terminal WebSocket endpoint.
- **Outputs:** Transmitted `ORDER_CREATE`, `ORDER_UPDATE`, `TABLE_QUERY` payloads.
- **Verification Criteria:**
  - Complete order creation and kitchen ticket dispatch succeeds during total WAN outage.
  - Zero attempt to establish outbound WAN TCP connections.
- **Fulfillment:** Spec: `SPEC-HANDHELD-MOBILE.md` | Contract: `CONTRACT-LAN-WEBSOCKET.md` | Test: `TC-HND-002`

### FR-HND-003: Tableside Tap-to-Pay & Digital Receipt Dispatch
- **Priority:** P1 (Target v1.0)
- **Source:** PRD HND-3 | SRS §4.3
- **User Story:** [US-HND-003](../USER_STORIES.md#story-us-hnd-003-tableside-tap-to-pay-and-digital-receipt-delivery)
- **Description:** The handheld shall interface with built-in EMV/NFC hardware sleds to accept contactless card payments, prompt guests for tip percentages (18%, 20%, 22%, Custom), and allow sending SMS or email receipts via the cloud or triggering a paper receipt at the counter.
- **Inputs:** Contactless card tap, tip percentage selection, guest phone/email.
- **Outputs:** Payment authorization event, digital receipt dispatch payload.
- **Verification Criteria:**
  - Payment authorization completes and check updates to `PAID` within 3 seconds.
  - Counter receipt or digital dispatch executes immediately upon approval.
- **Fulfillment:** Spec: `SPEC-HANDHELD-MOBILE.md` | Contract: `SCHEMA-PAYMENT-RECORD.md` | Test: `TC-HND-003`

---

## 3. Cross-Reference & Traceability

| Requirement ID | User Story | Contract / Schema | Architecture | Verification Test |
| :--- | :--- | :--- | :--- | :--- |
| `FR-HND-001` | `US-HND-001` | `EVENT-ORDER-CREATE` | `SPEC-HANDHELD-MOBILE.md` | `TC-HND-001` |
| `FR-HND-002` | `US-HND-002` | `CONTRACT-LAN-WEBSOCKET`| `SPEC-HANDHELD-MOBILE.md` | `TC-HND-002` |
| `FR-HND-003` | `US-HND-003` | `SCHEMA-PAYMENT-RECORD` | `SPEC-HANDHELD-MOBILE.md` | `TC-HND-003` |

---
_Document: REQ-03-HANDHELD.md | Node: REQUIREMENTS/FUNCTIONAL | Parent: [SRS_INDEX.md](../SRS_INDEX.md)_
