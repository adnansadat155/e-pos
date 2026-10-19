# Functional Requirements: Dinevique Cloud (Admin & Gateway)

**Document ID:** `REQ-05-CLOUD`  
**Parent:** [SRS_INDEX.md](../SRS_INDEX.md)  
**Source Ref:** [SYSTEM_PRD.md](../../SOURCES/SYSTEM_PRD.md) §5.5, [SYSTEM_SRS.md](../../SOURCES/SYSTEM_SRS.md) §4.5  
**Module:** Dinevique Cloud (Merchant Admin & Node.js Gateway)  
**Icon:** `cloud` | **Accent:** Muted Mid (`#6B7280`)  
**Status:** DEFINED  

---

## Table of Contents
- [1. Module Overview](#1-module-overview)
- [2. Detailed Requirements](#2-detailed-requirements)
  - [FR-CLD-001: Hierarchical Menu & Modifier Studio](#fr-cld-001-hierarchical-menu--modifier-studio)
  - [FR-CLD-002: Idempotent Event Ingestion Engine](#fr-cld-002-idempotent-event-ingestion-engine)
  - [FR-CLD-003: Real-Time Sales, Tender & Terminal Analytics](#fr-cld-003-real-time-sales-tender--terminal-analytics)
- [3. Cross-Reference & Traceability](#3-cross-reference--traceability)

---

## 1. Module Overview
Dinevique Cloud combines a Next.js administrative back-office portal with a high-throughput Node.js/Express API Gateway and a MongoDB document store. It acts as the canonical multi-tenant cloud master for menus, pricing overrides, user permissions, event stream ingestion, and real-time operational analytics across all restaurant locations.

---

## 2. Detailed Requirements

### FR-CLD-001: Hierarchical Menu & Modifier Studio
- **Priority:** P0 (Must Have)
- **Source:** PRD CLD-1 | SRS §4.5
- **User Story:** [US-CLD-001](../USER_STORIES.md#story-us-cld-001-centralized-hierarchical-menu-studio--instant-edge-sync)
- **Description:** Operators shall be able to create and edit deep hierarchical menu documents (`Menus → Categories → Items → Modifier Groups → Modifier Options → Nested Modifiers`), station routing assignments, and tax categories. Publishing menu changes shall dispatch a `MENU_SYNC` payload down active WSS tunnels to update edge Master Terminals.
- **Inputs:** Form input, drag-and-drop hierarchy reordering, "Publish" CTA.
- **Outputs:** Updated MongoDB menu document, outbound `MENU_SYNC` WebSocket broadcast.
- **Verification Criteria:**
  - Published menu changes reflect in edge Room SQLite within 2000ms.
  - Modifier nesting supports up to 3 hierarchical levels with min/max rules.
- **Fulfillment:** Spec: `SPEC-CLOUD-ADMIN.md` | Contract: `EVENT-MENU-SYNC.md` | Test: `TC-CLD-001`

### FR-CLD-002: Idempotent Event Ingestion Engine
- **Priority:** P0 (Must Have)
- **Source:** PRD CLD-2 | SRS §4.5
- **User Story:** [US-CLD-002](../USER_STORIES.md#story-us-cld-002-idempotent-cloud-event-ingestion-engine)
- **Description:** The Node.js API Gateway shall accept replayed offline event batches from Master Terminals. Every incoming event shall be evaluated by its client-generated `clientEventUuid`. Replays shall execute MongoDB `$set` upserts with timestamp comparison, guaranteeing zero duplicate orders or double-charged records upon retransmission.
- **Inputs:** HTTP POST / WSS frame containing array of queued event objects.
- **Outputs:** Batch ingestion ACK with processed UUID list.
- **Verification Criteria:**
  - Replaying 100 identical events results in exactly 100 database records (100% deduplication).
  - Processing throughput achieves >= 500 events/second per Node.js worker instance.
- **Fulfillment:** Spec: `SPEC-CLOUD-GATEWAY.md` | Contract: `CONTRACT-IDEMPOTENCY.md` | Test: `TC-CLD-002`

### FR-CLD-003: Real-Time Sales, Tender & Terminal Analytics
- **Priority:** P0 (Must Have)
- **Source:** PRD CLD-3 | SRS §4.5
- **User Story:** [US-CLD-003](../USER_STORIES.md#story-us-cld-003-real-time-sales-tender--terminal-heartbeat-analytics)
- **Description:** The admin portal shall display real-time aggregated metrics: gross/net sales, sales tax collected, tip totals, tender breakdowns (Cash, Card, Offline Queued), and live terminal connectivity status updated via 15-second heartbeat pings.
- **Inputs:** Periodic terminal heartbeats, committed order transaction documents.
- **Outputs:** Aggregated analytics graphs, terminal health indicator chips.
- **Verification Criteria:**
  - Revenue dashboards reflect new sales within 3000ms of cloud ingestion.
  - Terminal connection loss is visually indicated within 45 seconds (3 missed heartbeats).
- **Fulfillment:** Spec: `SPEC-CLOUD-ADMIN.md` | Contract: `API-ANALYTICS-SUMMARY.md` | Test: `TC-CLD-003`

---

## 3. Cross-Reference & Traceability

| Requirement ID | User Story | Contract / Schema | Architecture | Verification Test |
| :--- | :--- | :--- | :--- | :--- |
| `FR-CLD-001` | `US-CLD-001` | `EVENT-MENU-SYNC` | `SPEC-CLOUD-ADMIN.md` | `TC-CLD-001` |
| `FR-CLD-002` | `US-CLD-002` | `CONTRACT-IDEMPOTENCY` | `SPEC-CLOUD-GATEWAY.md` | `TC-CLD-002` |
| `FR-CLD-003` | `US-CLD-003` | `API-ANALYTICS-SUMMARY` | `SPEC-CLOUD-ADMIN.md` | `TC-CLD-003` |

---
_Document: REQ-05-CLOUD.md | Node: REQUIREMENTS/FUNCTIONAL | Parent: [SRS_INDEX.md](../SRS_INDEX.md)_
