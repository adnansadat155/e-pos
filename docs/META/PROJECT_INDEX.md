# Dinevique — Project Index

**Version:** 1.0.0
**Created:** 2026-10-19
**Agent Entry Point #1** — Read this first to understand the entire KB layout.

---

## 1. Project Summary

| Field | Value |
|---|---|
| **Project Name** | Dinevique Platform |
| **Brand Tagline** | *The Art of Smarter Dining.* |
| **Architecture Paradigm** | Native Android Master Terminal Edge Topology + Next.js / Node.js / MongoDB Cloud |
| **Platform Modules** | Dinevique POS, Dinevique Kitchen, Dinevique Handheld, Dinevique Online, Dinevique Cloud |
| **Primary Language (Edge)** | Kotlin (Jetpack Compose, Ktor, Android Room) |
| **Primary Language (Cloud)** | TypeScript (Next.js, Node.js/Express, Mongoose ODM) |
| **Database (Edge)** | Android Room / SQLite |
| **Database (Cloud)** | MongoDB (Self-hosted or Atlas) |
| **DDADS Version** | 1.1.0 |

---

## 2. Knowledge Base Map

| Node | Directory | Purpose | Entry File |
|---|---|---|---|
| **NODE 1 — Sources** | `docs/SOURCES/` | Original input documents (PRD, Brand, Architecture, Profile) | [SOURCES_INDEX.md](../SOURCES/SOURCES_INDEX.md) |
| **NODE 2 — Meta** | `docs/META/` | Project orientation and agent bootstrapping | This file + [AGENT_MANIFEST.md](AGENT_MANIFEST.md) |
| **NODE 3 — Requirements** | `docs/REQUIREMENTS/` | What the system must do | [SRS_INDEX.md](../REQUIREMENTS/SRS_INDEX.md) |
| **NODE 4 — Architecture** | `docs/ARCHITECTURE/` | How the system is designed | [ARCHITECTURE_INDEX.md](../ARCHITECTURE/ARCHITECTURE_INDEX.md) |
| **NODE 5 — Contracts** | `docs/CONTRACTS/` | Inviolable interfaces (API, events, schemas) | [CONTRACTS_INDEX.md](../CONTRACTS/CONTRACTS_INDEX.md) |
| **NODE 6 — Specs** | `docs/SPECS/` | How each module is built | [MODULE_SPECS_INDEX.md](../SPECS/MODULE_SPECS_INDEX.md) |
| **NODE 7 — Implementation** | `docs/IMPLEMENTATION/` | Sprint planning, roadmaps, audits | [IMPLEMENTATION_LOG.md](../IMPLEMENTATION/IMPLEMENTATION_LOG.md) |
| **Testing** | `docs/TESTING/` | QA strategy and test plans | [TESTING_INDEX.md](../TESTING/TESTING_INDEX.md) |
| **KB** | `docs/KB/` | De-duplicated canonical fact clusters | [KB_INDEX.md](../KB/KB_INDEX.md) |

---

## 3. Navigation Pattern

```
META/PROJECT_INDEX.md (YOU ARE HERE)
    ↓
META/AGENT_MANIFEST.md (current state + next action)
    ↓
IMPLEMENTATION/IMPLEMENTATION_LOG.md (what's been built)
    ↓
IMPLEMENTATION/PHASES/DEPENDENCY_GRAPH.md (phase ordering)
    ↓
Active Sprint directory
    ↓
Cross-links to SPECS/, CONTRACTS/, ARCHITECTURE/, REQUIREMENTS/
```

---

## 4. Technology Stack

### Edge Tier (Android)
- **Language:** Kotlin
- **UI:** Jetpack Compose + Kotlin Flows
- **Embedded Server:** Ktor (HTTP + WebSocket Foreground Service)
- **Local DB:** Android Room over SQLite
- **Hardware I/O:** UsbManager, Epson ePOS SDK, StarPRNT SDK

### Cloud Tier
- **Web/Admin:** Next.js (SSR + React)
- **API Gateway:** Node.js + Express
- **ODM:** Mongoose
- **Database:** MongoDB (Self-hosted or Atlas)
- **Realtime:** WebSocket tunnels (WSS)

### Cross-Cutting
- **Event IDs:** Client-generated UUIDs (idempotency)
- **Sync Protocol:** Store-and-forward FIFO queue with idempotent upserts
- **Icon Library:** Lucide (outlined, 2px stroke)
- **Typography:** Inter (UI) + Playfair Display (marketing)

---

## 5. Module Inventory

| Module | Technical Component | Epic ID | Phase |
|---|---|---|---|
| Dinevique POS | Native Android Master Terminal | Epic 1 | Phase 01 |
| Dinevique Kitchen | Native Android KDB | Epic 2 | Phase 01 |
| Dinevique Handheld | Native Android Mobile POS | Epic 3 | Phase 02 |
| Dinevique Online | Next.js SSR Storefront | Epic 4 | Phase 02 |
| Dinevique Cloud | Next.js Admin + Node.js Gateway + MongoDB | Epic 5 | Phase 01 |

---

## 6. Phase Overview

| Phase | Title | Focus | Status |
|---|---|---|---|
| **Phase 01** | Core Foundation | Monorepo setup, Android project scaffold, Cloud scaffold, data contracts, local DB, embedded Ktor server, KDB client | 🟢 Completed (100% Audit) |
| **Phase 02** | In-Store Operations | POS UI (table map, order entry, modifiers), Handheld UI, hardware I/O (printer, cash drawer), split checks | 🟢 Completed (100% Audit) |
| **Phase 03** | Cloud & Omnichannel | Dinevique Online storefront, Dinevique Cloud admin, menu studio, WSS sync | 🟡 Active |
| **Phase 04** | Resilience & Payments | Offline payments, store-and-forward drain, idempotent cloud ingestion, payment certification prep | Not Started |
| **Phase 05** | Polish & Governance | Brand compliance pass, WCAG audit, performance SLA validation, multi-location testing | Not Started |

---

_Document: PROJECT_INDEX.md | Node: META | Version: 1.0.0 | Created: 2026-10-19_
