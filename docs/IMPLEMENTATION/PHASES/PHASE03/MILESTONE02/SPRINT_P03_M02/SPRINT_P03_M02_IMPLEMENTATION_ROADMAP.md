# Sprint Implementation Roadmap: SPRINT_P03_M02

**Sprint ID:** `SPRINT_P03_M02`  
**Milestone:** Phase 03 — Milestone 02: Live Order Status Tracker & Analytics  
**Parent Milestone Index:** [MILESTONE02_INDEX.md](../MILESTONE02_INDEX.md)  
**Status:** 🟡 **ACTIVE**  
**Estimated Velocity:** 5 Tasks | 100% Passing Target  

---

## 1. Sprint Objective

Empower customers and restaurant operators with real-time visibility across the entire order lifecycle:
1. Provide an ultra-responsive Next.js customer live tracker (`/track/[orderUuid]`) powered by Server-Sent Events (SSE) that updates dynamically across four status stages (`ORDER_PLACED` $\rightarrow$ `IN_PREPARATION` $\rightarrow$ `READY_FOR_PICKUP` $\rightarrow$ `COMPLETED`) with $< 1000\text{ms}$ latency.
2. Provide an executive and operational analytics suite (`/admin/analytics`) aggregating gross sales, net revenue, average check size, category distribution, hourly transaction heatmaps, and labor cost ratios.
3. Deliver an enterprise Edge Terminal Monitor (`/admin/terminals`) detailing active edge instances, Ktor LAN port status, mDNS reachability, offline sync queue depth, and connected hardware peripheral status.
4. Build the resilient backend SSE broadcasting hub and telemetry controllers in `@dinevique/gateway`.
5. Verify all real-time SLAs, calculations, and brand tokens via an automated test suite.

---

## 2. Work Breakdown Structure (WBS)

### Task T01: Customer Live Order Status Tracker UI (`/track/[orderUuid]`)
- **Location:** `apps/web/app/track/[orderUuid]/page.tsx`
- **Requirements:**
  - 4-stage animated progression stepper (`ORDER_PLACED`, `IN_PREPARATION`, `READY_FOR_PICKUP`, `COMPLETED`).
  - Real-time Server-Sent Events (SSE) listener connecting to `/api/v1/orders/:orderUuid/stream`.
  - Polling fallback if SSE connection disconnects or fails.
  - Prominent 4-digit pickup code display and estimated preparation countdown timer.
  - Itemized receipt accordion detailing items, modifier options, subtotal, tax, tip, and total paid.
  - Strict Brand Token compliance: Brand Onyx (`#0D0D0D`), Brand Slate (`#1E2A38`), Brand Amber (`#D4820A`), Brand Ink (`#1A1A1A`), Inter typography, Lucide outline icons, 0 exclamation marks.

### Task T02: Cloud Analytics & Operations Dashboard (`/admin/analytics`)
- **Location:** `apps/web/app/admin/analytics/page.tsx`
- **Requirements:**
  - KPI summary metric cards: Gross Sales, Net Revenue, Orders Count, Average Check Size, and Labor Cost Ratio.
  - Sales by Category visualizer with percentage breakdowns.
  - Hourly Order Heatmap / Volume distribution matrix (Peak hours: Lunch 12–2pm, Dinner 6–9pm).
  - Date range filters (Today, Yesterday, Last 7 Days, Month to Date).
  - WCAG AA contrast pairing: Brand Amber buttons with Brand Ink text.

### Task T03: Master Terminal Heartbeat & Edge Health Monitor (`/admin/terminals`)
- **Location:** `apps/web/app/admin/terminals/page.tsx`
- **Requirements:**
  - Active terminal grid displaying terminal ID, role (`MASTER_TERMINAL`, `HANDHELD_ORDER_PAD`, `KDB_SCREEN`), IP address, LAN latency ping (ms), and last heartbeat timestamp.
  - Offline sync queue indicator displaying pending offline transactions awaiting cloud reconciliation.
  - Peripheral health matrix: Thermal Receipt Printer (Paper OK / Low / Out), Kitchen Impact Printer (Ribbon OK / Warning), Solenoid Cash Drawer (Closed / Open).
  - Manual "Ping LAN Fleet" trigger with simulated latency telemetry.

### Task T04: Cloud Gateway Live SSE Broadcast & Telemetry Controllers
- **Location:** `apps/cloud-gateway/src/controllers/OrderTrackerController.ts`, `TerminalHeartbeatController.ts`, `AnalyticsController.ts`, `routes/api.ts`
- **Requirements:**
  - `GET /api/v1/orders/:orderUuid/stream`: SSE response stream keeping connection open (`text/event-stream`, `keep-alive`) and sending instant status push upon update.
  - `POST /api/v1/orders/:orderUuid/status`: Updates order status in repository and emits `ORDER_UPDATED` event to all active SSE subscribers for that order UUID within $< 1000\text{ms}$.
  - `POST /api/v1/terminals/heartbeat`: Ingests edge terminal health payload (LAN IP, latency, sync queue depth, battery, peripheral state).
  - `GET /api/v1/terminals`: Returns list of all active terminals and their health metrics.
  - `GET /api/v1/analytics/summary`: Computes aggregate sales figures, category share, and hourly heatmaps from ingested orders.

### Task T05: Automated Verification Test Suite
- **Location:** `apps/cloud-gateway/test/order-tracker-and-analytics.test.mjs`
- **Requirements:**
  - Validate SSE connection establishment and event push with latency benchmark $< 500\text{ms}$.
  - Validate 4-stage order lifecycle transitions.
  - Validate terminal heartbeat registration and offline queue depth reporting.
  - Validate analytics aggregation mathematics (Gross, Net, Category Share, Average Check).

---

## 3. Strict Acceptance Criteria & SLAs

| Criteria | Target SLA / Standard | Verification Method |
| :--- | :--- | :--- |
| SSE Push Latency | $< 1000\text{ms}$ from kitchen status update to customer UI | Automated test in Node runner |
| Analytics Math Accuracy | Penny-perfect $\sum \text{Item} + \text{Tax} \equiv \text{Gross}$ | Unit test assertion |
| Terminal Health Telemetry | Latency ping $< 50\text{ms}$ LAN | Heartbeat API response check |
| Brand Guidelines & Voice | 0 exclamation marks, `#D4820A` paired with `#1A1A1A` | Codebase grep & visual inspection |
| Web Build Integrity | 0 TypeScript/Next.js build errors | `pnpm --filter @dinevique/web build` |

---
_Sprint Roadmap: SPRINT_P03_M02 | Phase 03: Cloud & Omnichannel | Status: ACTIVE_
