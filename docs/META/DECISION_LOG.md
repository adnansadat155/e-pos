# Architecture Decision Log (ADR) — Dinevique Platform

**Parent:** [PROJECT_INDEX.md](PROJECT_INDEX.md)  
**Version:** 1.0.0  
**Last Updated:** 2026-10-19  

---

## 1. Decision Registry

| ADR ID | Date | Status | Title | Deciders | Source Doc |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **ADR-001** | 2026-10-19 | ACCEPTED | Adopt DDADS v1.1.0 as Software Development Lifecycle Control Plane | Engineering Lead, Architecture Team | [SYSTEM_PRD.md](../SOURCES/SYSTEM_PRD.md) |
| **ADR-002** | 2026-10-19 | ACCEPTED | Primary Edge Architecture: Native Android Master Terminal with Embedded Ktor | System Architect | [SYSTEM_ARCHITECTURE_SPEC.md](../SOURCES/SYSTEM_ARCHITECTURE_SPEC.md) |
| **ADR-003** | 2026-10-19 | ACCEPTED | Store-and-Forward FIFO Event Queue with Client-Generated UUID Idempotency | Principal Data Architect | [SYSTEM_PRD.md](../SOURCES/SYSTEM_PRD.md) §4.3 |
| **ADR-004** | 2026-10-19 | ACCEPTED | Strict Brand Enforcement (Inter + WCAG AA 4.5:1 Brand Amber Fill Pairing) | Brand & Design Lead | [SYSTEM_BRAND_AND_STYLE_GUIDELINE.md](../SOURCES/SYSTEM_BRAND_AND_STYLE_GUIDELINE.md) |

---

## 2. Detailed Decision Records

### ADR-001: Adopt DDADS v1.1.0 as SDLC Control Plane
- **Context:** Large-scale multi-tier point-of-sale platform requires autonomous agentic execution with strict document-before-code rigor and zero hallucinated drift.
- **Decision:** All engineering workflows follow the Document-Driven Agentic Development System (DDADS). Documentation serves as the single source of truth.
- **Consequences:** Implementation roadmaps, technical plans, and audit reports are strictly mandatory before writing code.

### ADR-002: Native Android Master Terminal with Embedded Ktor
- **Context:** Traditional restaurant POS deployments rely on external Windows server boxes or fragile direct-to-cloud connections, causing dropped tickets and sync failures during network outages.
- **Decision:** Eliminate dedicated PC servers. Run a lightweight embedded Ktor HTTP/WebSocket server directly within an Android Foreground Service on the primary counter terminal (Dinevique POS).
- **Consequences:** Lowers hardware footprint and cost. Edge operations continue at full capacity even if WAN internet disconnects.

### ADR-003: Store-and-Forward FIFO Queue with Client-Generated UUIDs
- **Context:** Offline orders and state changes must sync reliably to MongoDB without data loss or duplicate charges when WAN reconnects.
- **Decision:** Edge Room SQLite queues mutations in `outbound_event_queue` and offline card authorizations in `offline_payment_queue`. Every event carries an edge-minted UUID. Cloud gateway applies idempotent `$set` upserts.
- **Consequences:** Guaranteed zero double-charges and 100% deduplication upon network replay.

---
_Document: DECISION_LOG.md | Node: META | Version: 1.0.0_
