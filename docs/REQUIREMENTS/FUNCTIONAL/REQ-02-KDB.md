# Functional Requirements: Dinevique Kitchen (Kitchen Display Board - KDB)

**Document ID:** `REQ-02-KDB`  
**Parent:** [SRS_INDEX.md](../SRS_INDEX.md)  
**Source Ref:** [SYSTEM_PRD.md](../../SOURCES/SYSTEM_PRD.md) §5.2, [SYSTEM_SRS.md](../../SOURCES/SYSTEM_SRS.md) §4.2  
**Module:** Dinevique Kitchen (Native Android KDB)  
**Icon:** `monitor` | **Accent:** Info Blue (`#2563EB`)  
**Status:** DEFINED  

---

## Table of Contents
- [1. Module Overview](#1-module-overview)
- [2. Detailed Requirements](#2-detailed-requirements)
  - [FR-KDB-001: Real-Time LAN WebSocket Ticket Stream](#fr-kdb-001-real-time-lan-websocket-ticket-stream)
  - [FR-KDB-002: Ticket State Transitions & Bump Workflow](#fr-kdb-002-ticket-state-transitions--bump-workflow)
  - [FR-KDB-003: Station Filtering, Aging Timers & Bump Recall](#fr-kdb-003-station-filtering-aging-timers--bump-recall)
  - [FR-KDB-004: Live 86ing from the Pass](#fr-kdb-004-live-86ing-from-the-pass)
- [3. Cross-Reference & Traceability](#3-cross-reference--traceability)

---

## 1. Module Overview
Dinevique Kitchen operates as an Android client mounted on kitchen touchscreens or paired with physical bump bars. Connected directly to the Master Terminal over local LAN WebSockets, it renders incoming kitchen tickets on a Brand Onyx (`#0D0D0D`) dark canvas, manages station routing, tracks cook SLAs, and coordinates ticket state transitions.

---

## 2. Detailed Requirements

### FR-KDB-001: Real-Time LAN WebSocket Ticket Stream
- **Priority:** P0 (Must Have)
- **Source:** PRD KDB-1 | SRS §4.2
- **User Story:** [US-KDB-001](../USER_STORIES.md#story-us-kdb-001-sub-400ms-ticket-stream--high-contrast-visual-alerts)
- **Description:** The KDB client shall maintain a persistent WebSocket client connection to the Master Terminal's Ktor server. It shall ingest `TICKET_RECEIVED` events and render incoming orders on high-contrast cards (`#1E2A38` frame, `#2D7D46` New Ticket badge) with ticket number, table label, server name, items, and modifier notes.
- **Inputs:** Inbound JSON WebSocket frame `TICKET_RECEIVED`.
- **Outputs:** Updated Jetpack Compose UI state, optional audio alert chime.
- **Verification Criteria:**
  - Ticket renders on screen in < 400ms from the moment the Master Terminal processes the order.
  - UI maintains 60fps scrolling and renders properly on 10-inch to 24-inch screens.
- **Fulfillment:** Spec: `SPEC-KITCHEN-KDB.md` | Contract: `EVENT-TICKET-RECEIVED.md` | Test: `TC-KDB-001`

### FR-KDB-002: Ticket State Transitions & Bump Workflow
- **Priority:** P0 (Must Have)
- **Source:** PRD KDB-2 | SRS §4.2
- **User Story:** [US-KDB-002](../USER_STORIES.md#story-us-kdb-002-ticket-progression--micro-interaction-bump-workflow)
- **Description:** Cooks shall transition tickets across states: `NEW TICKET` (Success Green `#2D7D46`) → `IN PROGRESS` (Brand Amber `#D4820A`) → `BUMPED` via touch or bump bar input. Tapping `BUMP ->` triggers a 100ms Brand Amber flash before dismissal and emits a `TICKET_BUMPED` event back to the Master Terminal.
- **Inputs:** Touch tap on bump CTA or physical bump bar keypress (Numbers 1–8).
- **Outputs:** State mutation, visual 100ms flash, outbound `TICKET_BUMPED` event.
- **Verification Criteria:**
  - Visual bump flash occurs in <= 100ms.
  - Master Terminal receives `TICKET_BUMPED` over WebSocket in < 150ms.
- **Fulfillment:** Spec: `SPEC-KITCHEN-KDB.md` | Contract: `EVENT-TICKET-BUMPED.md` | Test: `TC-KDB-002`

### FR-KDB-003: Station Filtering, Aging Timers & Bump Recall
- **Priority:** P1 (Target v1.0)
- **Source:** PRD KDB-3 | SRS §4.2
- **User Story:** [US-KDB-003](../USER_STORIES.md#story-us-kdb-003-station-specific-filtering--60-minute-ticket-recall)
- **Description:** The KDB shall support filtering tickets by prep station (*Grill*, *Fry*, *Salad*, *Pastry*, *Expo*). Tickets shall render an elapsed timer that transitions color thresholds: White/Linen (< 10 min) → Amber (10–20 min) → Red (> 20 min). Cooks shall be able to reopen any bumped ticket within 60 minutes from a slide-out Recall Drawer.
- **Inputs:** Station filter toggle, timer tick, Recall drawer tap.
- **Outputs:** Filtered ticket list, visual timer warning styles, restored ticket broadcast.
- **Verification Criteria:**
  - Multi-station orders display only relevant station items on designated station terminals.
  - Bumped tickets remain restorable for 60 minutes.
- **Fulfillment:** Spec: `SPEC-KITCHEN-KDB.md` | Contract: `EVENT-TICKET-RECALLED.md` | Test: `TC-KDB-003`

### FR-KDB-004: Live 86ing from the Pass
- **Priority:** P1 (Target v1.0)
- **Source:** PRD KDB-4 | SRS §4.2
- **User Story:** [US-KDB-004](../USER_STORIES.md#story-us-kdb-004-rapid-86ing-from-the-pass-with-global-propagation)
- **Description:** Kitchen leads shall be able to mark any menu item or modifier option as out-of-stock (86ed) directly on the KDB. This event shall emit to the Master Terminal, which instantly propagates the disabled state across all in-store Handhelds, POS terminals, and cloud storefronts.
- **Inputs:** Item selection in 86 modal, confirm toggle.
- **Outputs:** Outbound `ITEM_86ED` WebSocket event.
- **Verification Criteria:**
  - Disabled item appears non-selectable across all devices within 1000ms.
- **Fulfillment:** Spec: `SPEC-KITCHEN-KDB.md` | Contract: `EVENT-ITEM-86.md` | Test: `TC-KDB-004`

---

## 3. Cross-Reference & Traceability

| Requirement ID | User Story | Contract / Schema | Architecture | Verification Test |
| :--- | :--- | :--- | :--- | :--- |
| `FR-KDB-001` | `US-KDB-001` | `EVENT-TICKET-RECEIVED` | `SPEC-KITCHEN-KDB.md` | `TC-KDB-001` |
| `FR-KDB-002` | `US-KDB-002` | `EVENT-TICKET-BUMPED` | `SPEC-KITCHEN-KDB.md` | `TC-KDB-002` |
| `FR-KDB-003` | `US-KDB-003` | `EVENT-TICKET-RECALLED` | `SPEC-KITCHEN-KDB.md` | `TC-KDB-003` |
| `FR-KDB-004` | `US-KDB-004` | `EVENT-ITEM-86` | `SPEC-KITCHEN-KDB.md` | `TC-KDB-004` |

---
_Document: REQ-02-KDB.md | Node: REQUIREMENTS/FUNCTIONAL | Parent: [SRS_INDEX.md](../SRS_INDEX.md)_
