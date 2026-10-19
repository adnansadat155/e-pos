# Sprint Implementation Roadmap: SPRINT_P01_M02

**Parent:** [MILESTONE02_INDEX.md](../MILESTONE02_INDEX.md)  
**Sprint:** Phase 01 / Milestone 02 / Sprint 2  
**Title:** Ktor LAN Server, Edge Room SQLite Database & KDB Real-Time Ticket Pipeline  
**Start Date:** 2026-10-19  
**Status:** 🟡 Ready for Execution  

---

## 1. Sprint Objective
Build the reactive edge data pipeline and in-store communication hub on the Native Android Master Terminal:
1. Complete compile-time verified Android Room SQLite DAOs, TypeConverters, and reactive Kotlin Flow publishers for orders, outbound queues, and offline payments.
2. Implement the embedded Ktor HTTP & WebSocket server engine with connection clustering, message routing, and frame serialization.
3. Implement Android Network Service Discovery (mDNS) registering `_dinevique-pos._tcp.local.` on the Master Terminal and automatic client resolution on KDB and Handheld.
4. Connect the Dinevique Kitchen (KDB) client to receive live `TICKET_RECEIVED` events and emit `TICKET_BUMPED` mutations.
5. Create an automated integration test suite validating sub-400ms ticket delivery over local LAN sockets.

---

## 2. Ordered Task Breakdown

| Task ID | Title | Complexity | Dependencies | Requirements Traced | Verification Method |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **T01** | Room SQLite Database DAOs & Flow Observables | Medium | None | `FR-POS-003`, `FR-POS-006` | Unit & Room DAO instrumentation tests |
| **T02** | Ktor HTTP & WebSocket Server Routing Engine | High | T01 | `FR-POS-001`, `FR-KDB-001` | Server route & WebSocket socket test |
| **T03** | Local mDNS Service Discovery (NSD) Engine | Medium | T02 | `FR-POS-001` | Discovery resolution simulation (< 2s) |
| **T04** | KDB Client Integration & Ticket Event Pipeline | High | T02, T03 | `FR-KDB-001`, `FR-KDB-002` | KDB UI state emission & bump test |
| **T05** | LAN Event Loop Integration Testing & Latency SLA | High | T01–T04 | `NFR-PERF-002`, `NFR-REL-001`| LAN round-trip benchmark (< 400ms) |

---

## 3. Detailed Task Specifications

### Task T01: Room SQLite Database DAOs & Flow Observables
- **Objective:** Finalize `DineviqueDatabase` with complete DAOs, transaction support, Room `TypeConverter` implementations for JSON string serialization, and reactive `Flow<List<LocalOrderEntity>>` streams.
- **Deliverables:**
  - `apps/android/core-database/src/main/java/com/dinevique/core/database/converters/Converters.kt`
  - `apps/android/core-database/src/main/java/com/dinevique/core/database/dao/OrderDao.kt`
  - `apps/android/core-database/src/main/java/com/dinevique/core/database/dao/OutboundQueueDao.kt`
  - `apps/android/core-database/src/main/java/com/dinevique/core/database/dao/OfflinePaymentDao.kt`

### Task T02: Ktor HTTP & WebSocket Server Routing Engine
- **Objective:** Build full message routing inside `KtorLanServer.kt` to parse inbound JSON envelopes (`ORDER_CREATE`, `TICKET_BUMPED`, `ITEM_86ED`), commit transactions to Room SQLite, and broadcast updates to connected KDB clients.
- **Deliverables:**
  - `apps/android/core-network/src/main/java/com/dinevique/core/network/KtorLanServer.kt` (enhanced router)
  - `apps/android/core-network/src/main/java/com/dinevique/core/network/models/LanMessages.kt`

### Task T03: Local mDNS Service Discovery (NSD) Engine
- **Objective:** Implement `NsdBroadcastManager.kt` on the Master Terminal to announce `_dinevique-pos._tcp.local.` and `NsdDiscoveryManager.kt` on auxiliary devices to auto-discover Master IP without manual setup.
- **Deliverables:**
  - `apps/android/core-network/src/main/java/com/dinevique/core/network/discovery/NsdBroadcastManager.kt`
  - `apps/android/core-network/src/main/java/com/dinevique/core/network/discovery/NsdDiscoveryManager.kt`

### Task T04: KDB Client Integration & Ticket Event Pipeline
- **Objective:** Integrate `LanWebSocketClient` into `app-kitchen`, binding incoming `TICKET_RECEIVED` events to Compose UI state and dispatching `TICKET_BUMPED` events back to the Master Terminal.
- **Deliverables:**
  - `apps/android/app-kitchen/src/main/java/com/dinevique/kitchen/KitchenViewModel.kt`
  - Update `apps/android/app-kitchen/src/main/java/com/dinevique/kitchen/KitchenMainActivity.kt`

### Task T05: LAN Event Loop Integration Testing & Latency SLA
- **Objective:** Develop an end-to-end integration test and automated event loop runner validating that an `ORDER_CREATE` sent to Ktor propagates to KDB in < 400ms and commits to Room DB.
- **Deliverables:**
  - `packages/contracts/test/lan-event-loop.test.mjs`
  - Verification checklist and timing asserts.

---
_Document: SPRINT_P01_M02_IMPLEMENTATION_ROADMAP.md | Sprint: SPRINT_P01_M02 | Parent: [MILESTONE02_INDEX.md](../MILESTONE02_INDEX.md)_
