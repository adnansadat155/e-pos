# Sprint Implementation Roadmap: SPRINT_P01_M03

**Parent:** [MILESTONE03_INDEX.md](../MILESTONE03_INDEX.md)  
**Sprint:** Phase 01 / Milestone 03 / Sprint 3  
**Title:** Production Cloud Gateway, Bidirectional Edge WSS Tunnels & Idempotent Ingestion  
**Start Date:** 2026-10-19  
**Status:** 🟡 Ready for Execution  

---

## 1. Sprint Objective
Complete the cloud-side ingestion and communication backbone of Phase 01:
1. Implement Mongoose connection management with connection pooling, automatic reconnect with exponential backoff, and graceful shutdown.
2. Build the persistent encrypted WebSocket (`WSS`) edge tunnel manager inside `apps/cloud/gateway`:
   - Authentication handshake exchanging terminal UUID and restaurant token.
   - Heartbeat health monitoring (15s ping interval, 30s connection timeout).
   - Downlink push of web orders (`ORDER_CLOUD_INGEST`) down to edge Master Terminals.
   - Uplink relay of bump state transitions (`TICKET_BUMPED`) up from edge to cloud.
3. Build the store-and-forward batch ingestion engine executing MongoDB `$set` upserts with timestamp reconciliation, guaranteeing 100% deduplication upon replay.
4. Verify with automated stress tests and idempotency assertions.

---

## 2. Ordered Task Breakdown

| Task ID | Title | Complexity | Dependencies | Requirements Traced | Verification Method |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **T01** | Database Connection & Graceful Lifecycle Manager | Low | None | N/A | Connection pool & disconnect test |
| **T02** | Bidirectional WSS Edge Tunnel Connection Hub | High | T01 | `FR-ONL-002`, `FR-CLD-002` | Bi-directional socket test |
| **T03** | Batch Ingestion Controller & Deduplication Engine | High | T01, T02 | `FR-CLD-002`, `NFR-REL-005`| Idempotent batch test with replay |
| **T04** | Cloud Gateway Automated Stress & Idempotency Test | Medium | T01–T03 | `NFR-PERF-005` | Stress benchmark (50 replayed orders)|
| **T05** | Milestone 03 Compliance Audit & Phase 01 Closeout | Medium | T01–T04 | DDADS Quality Gates | 100% Audit Pass & Manifest Update |

---

## 3. Detailed Task Specifications

### Task T01: Database Connection & Graceful Lifecycle Manager
- **Deliverables:**
  - `apps/cloud/gateway/src/db.ts`
  - Connection pooling (minPoolSize: 5, maxPoolSize: 25) with retry logic.

### Task T02: Bidirectional WSS Edge Tunnel Connection Hub
- **Deliverables:**
  - `apps/cloud/gateway/src/tunnel/EdgeTunnelManager.ts`
  - Connection registry, heartbeat tracking, downlink dispatch (`sendToTerminal`).

### Task T03: Batch Ingestion Controller & Deduplication Engine
- **Deliverables:**
  - `apps/cloud/gateway/src/controllers/IngestController.ts`
  - Bulk upsert execution with client UUID deduplication.

### Task T04: Cloud Gateway Automated Stress & Idempotency Test
- **Deliverables:**
  - `apps/cloud/gateway/test/idempotency-stress.test.mjs`
  - Zero duplicate records asserted across multiple identical batch replays.

### Task T05: Milestone 03 Compliance Audit & Phase 01 Closeout
- **Deliverables:**
  - `SPRINT_P01_M03_IMPLEMENTATION_AUDIT_REPORT.md`
  - Update `IMPLEMENTATION_LOG.md` and `AGENT_MANIFEST.md` declaring Phase 01 Complete.

---
_Document: SPRINT_P01_M03_IMPLEMENTATION_ROADMAP.md | Sprint: SPRINT_P01_M03 | Parent: [MILESTONE03_INDEX.md](../MILESTONE03_INDEX.md)_
