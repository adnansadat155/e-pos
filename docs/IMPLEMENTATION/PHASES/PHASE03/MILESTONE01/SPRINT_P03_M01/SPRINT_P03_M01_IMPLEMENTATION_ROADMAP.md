# Sprint Implementation Roadmap: Phase 03 / Milestone 01

**Sprint ID:** `SPRINT_P03_M01`  
**Phase:** Phase 03 — Cloud & Omnichannel  
**Milestone:** Milestone 01 — Next.js Storefront & Cloud Menu Studio  
**Target Duration:** Sprint 1  
**Status:** 🟡 **IN EXECUTION**  
**Traceability:** PRD §5.4, §5.5, §6.1 | SRS §4.4, §4.5 | REQ-04-ONLINE (`FR-ONL-001`, `FR-ONL-002`), REQ-05-CLOUD (`FR-CLD-001`) | User Stories: `US-ONL-001`, `US-CLD-001`  

---

## 1. Objectives & Deliverables

This sprint delivers the public online customer experience and administrative menu studio:
1. **Interactive Next.js 14 Online Storefront (`/menu`):** Rich consumer interface with category navigation, product cards, dynamic modifier modal (enforcing min/max selections), and responsive sliding cart drawer with tax & total calculations.
2. **Cloud Admin Menu Studio (`/admin/menu`):** Administrative portal for restaurant managers to configure menu categories, items, prices, stations, and modifier hierarchies, with immediate edge deployment.
3. **Cloud API & Edge `MENU_SYNC` Engine (`MenuController.ts`):** High-throughput endpoints (`GET /api/v1/menu`, `POST /api/v1/menu`, `POST /api/v1/menu/publish`) broadcasting `MENU_SYNC` event envelopes over active WSS edge tunnels to Master Terminals.
4. **Automated Verification Test Suite (`storefront-menu.test.mjs`):** End-to-end tests covering menu persistence, edge WebSocket publication, and storefront cart math.

---

## 2. Task Breakdown

| Task ID | Task Name | Component | Scope / Details | Success Criteria |
| :--- | :--- | :--- | :--- | :--- |
| **T01** | Next.js Storefront | `apps/cloud/web` | Interactive menu, category chips, modifier modal, cart drawer | Full checkout flow with responsive cart math |
| **T02** | Cloud Menu Studio | `apps/cloud/web` | Category/item/modifier CRUD, live preview, publish CTA | Manage catalog & trigger edge publish |
| **T03** | Menu API Controller | `apps/cloud/gateway` | Express routes, MongoDB Mongoose persistence, edge tunnel broadcast | GET, POST, and PUBLISH endpoints operational |
| **T04** | WSS Edge Sync Hook | `apps/cloud/gateway` | WSS `MENU_SYNC` distribution across active terminals | Dispatches within < 500ms over active tunnels |
| **T05** | Test Suite & Validation | `test/storefront-menu.test.mjs` | API tests, cart calculations, WebSocket sync tests | 100% test pass rate & SLA compliance |

---

## 3. Strict Brand & Performance Standards

- **Aesthetics:** Dark luxury dining aesthetic using Brand Onyx (`#0D0D0D`), Brand Slate (`#1E2A38`), Brand Amber (`#D4820A`), Brand Ink (`#1A1A1A`), Inter typography, subtle borders, and smooth transitions.
- **Accessibility:** All Brand Amber buttons strictly pair with Brand Ink text (WCAG AA 4.5:1 compliant).
- **Edge Sync Latency:** Changes published from the Cloud Menu Studio must broadcast to Master Terminals within $< 500\text{ms}$.
- **Copy:** Zero exclamation marks across all online ordering and admin copy.

---
_Sprint Roadmap | Parent: [MILESTONE01_INDEX.md](../MILESTONE01_INDEX.md) | Status: APPROVED_
