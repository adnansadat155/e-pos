# Sprint Implementation Roadmap: SPRINT_P01_M01

**Parent:** [MILESTONE01_INDEX.md](../MILESTONE01_INDEX.md)  
**Sprint:** Phase 01 / Milestone 01 / Sprint 1  
**Title:** Monorepo Scaffolding, Core Project Setup & Shared Event Schemas  
**Start Date:** 2026-10-19  
**Status:** 🟡 Ready for Execution  

---

## 1. Sprint Objective
Initialize the root workspace, establish the Native Android Gradle project structure (supporting Jetpack Compose, Room SQLite, and embedded Ktor), scaffold the Cloud workspace (Next.js 14 App Router, Express Gateway, and Mongoose ODM), and define shared JSON event schemas matching Dinevique Brand Tokens.

---

## 2. Ordered Task Breakdown

| Task ID | Title | Complexity | Dependencies | Requirements Traced | Verification Method |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **T01** | Root Monorepo & Directory Scaffolding | Low | None | N/A | Directory check & workspace config |
| **T02** | Shared Data Contracts & Schemas | Medium | T01 | `FR-POS-001`, `FR-CLD-002` | Schema validator & JSON schema test |
| **T03** | Android Project Scaffold (Edge Tier) | High | T01, T02 | `FR-POS-001`, `FR-POS-002` | `./gradlew build` validation |
| **T04** | Cloud Node.js API Gateway Scaffold | Medium | T01, T02 | `FR-CLD-002` | `npm test` Express health route |
| **T05** | Dinevique Online & Cloud Next.js Scaffold | Medium | T01, T02 | `FR-ONL-001`, `FR-CLD-001` | `npm run build` Next.js SSR test |

---

## 3. Detailed Task Specifications

### Task T01: Root Monorepo & Directory Scaffolding
- **Objective:** Establish the clean monorepo structure separating edge (Android), cloud (Node.js/Next.js), shared contracts, and documentation.
- **Deliverables:**
  - `apps/android/` (Dinevique POS, Kitchen, Handheld)
  - `apps/cloud/` (Dinevique Online & Cloud Admin)
  - `packages/contracts/` (Shared JSON schemas & TypeScript types)
  - Root `package.json` with npm workspaces or pnpm.

### Task T02: Shared Data Contracts & Schemas
- **Objective:** Create canonical TypeScript interfaces and JSON Schema specifications for `Order`, `Menu`, `Ticket`, `OfflinePayment`, and WebSocket event envelopes (`ORDER_CREATE`, `TICKET_RECEIVED`, `TICKET_BUMPED`).
- **Deliverables:**
  - `packages/contracts/src/events.ts`
  - `packages/contracts/src/menu.ts`
  - `packages/contracts/src/order.ts`

### Task T03: Android Project Scaffold (Edge Tier)
- **Objective:** Initialize Android root `settings.gradle.kts` and `build.gradle.kts` targeting Android 14 (API 34) with minSdk 30, including dependencies for Jetpack Compose, Room 2.6, Ktor 2.3 embedded server, and Kotlin Serialization.
- **Deliverables:**
  - `apps/android/settings.gradle.kts`
  - `apps/android/app-pos/`
  - `apps/android/app-kitchen/`
  - `apps/android/app-handheld/`
  - `apps/android/core-database/` (Room SQLite)
  - `apps/android/core-network/` (Ktor Server & Client)

### Task T04: Cloud Node.js API Gateway Scaffold
- **Objective:** Initialize Express/Node.js service with WebSocket server support, Mongoose 8.x connection management, and `/health` ping route.
- **Deliverables:**
  - `apps/cloud/gateway/package.json`
  - `apps/cloud/gateway/src/server.ts`
  - `apps/cloud/gateway/src/config.ts`

### Task T05: Dinevique Online & Cloud Next.js Scaffold
- **Objective:** Initialize Next.js 14 App Router project with Tailwind CSS configured with exact Dinevique Brand Tokens (Brand Onyx `#0D0D0D`, Brand Amber `#D4820A`, Brand Slate `#1E2A38`, Brand Ink `#1A1A1A`, Inter font).
- **Deliverables:**
  - `apps/cloud/web/package.json`
  - `apps/cloud/web/tailwind.config.ts`
  - `apps/cloud/web/src/app/layout.tsx`

---

## 4. Verification & Quality Checklist
- [ ] Root monorepo directories created according to DDADS spec.
- [ ] Shared contract schemas build and export valid TypeScript types.
- [ ] Android Gradle configuration parses cleanly without syntax errors.
- [ ] Node.js Express gateway launches and passes health check test.
- [ ] Next.js app builds with correct Brand Tokens in CSS.

---
_Document: SPRINT_P01_M01_IMPLEMENTATION_ROADMAP.md | Sprint: SPRINT_P01_M01 | Parent: [MILESTONE01_INDEX.md](../MILESTONE01_INDEX.md)_
