# Product Requirements Document (PRD): Dinevique Platform (v1.0 MVP)

*The Art of Smarter Dining.*

| Document Metadata | Details |
| :--- | :--- |
| **Master Brand** | Dinevique |
| **Platform Modules** | Dinevique POS, Dinevique Kitchen, Dinevique Handheld, Dinevique Online, Dinevique Cloud |
| **Document Version** | v1.0 — Synthesized from Company Profile, E-POS MVP Architecture & Brand Guidelines v1.0 |
| **Date** | October 2026 |
| **Architecture Paradigm** | Native Android Master Terminal Edge Topology + Next.js / Node.js / MongoDB Cloud |
| **Status** | Draft — Approved for Engineering & Design Execution |

## 1. Executive Summary & Brand Foundation

### 1.1 Product Purpose & Vision
Dinevique exists to restore clarity to restaurant operations. Behind every dining experience is a team managing dozens of moving parts—orders, tickets, tables, kitchens, and payments—often with fragmented tools that break under network pressure.

* **Vision:** To help restaurants operate with greater clarity, resilience, and consistency through accessible, connected technology.
* **Mission:** To simplify restaurant workflows by connecting orders, service teams, kitchen operations, and management tools in a practical platform built for everyday service.
* **Brand Essence:** Connected clarity for every service.
* **Tagline:** *The Art of Smarter Dining.* (Alternative: *Every Order. One Connected Platform.*)

### 1.2 Core Product Pillars

| Pillar | Engineering & Product Commitment | Operator Promise |
| :--- | :--- | :--- |
| **One Platform** | Orders, kitchen, handheld, online storefront, and cloud administration operate on a single system of record. | Operators never need to reconcile data across separate tools. |
| **Operational Resilience** | Master Terminal Edge Topology continues ordering, printing, and kitchen coordination during Wide Area Network (WAN) outages. | The restaurant does not stop when the internet does. |
| **Team Clarity** | Every screen surfaces only what a team member needs right now, adhering to strict WCAG AA contrast and legibility standards. | Teams work faster because the platform gets out of the way. |
| **Scalable Foundation** | Modular architecture scales from a single front-counter tablet to multi-terminal, multi-location hospitality groups. | The technology operators start with is the technology they scale with. |

### 1.3 Modular Brand Architecture
Dinevique operates as a **single-brand, modular platform**. All modules inherit the master brand identity and carry a dedicated Lucide signature icon and semantic color chip for UI navigation and documentation:

| Platform Module | Technical Component | Signature Icon | Module Accent | Core Functional Scope |
| :--- | :--- | :--- | :--- | :--- |
| **Dinevique POS** | Native Android Master Terminal | `receipt` | Brand Amber (`#D4820A`) | Central front-of-house orchestrator for order entry, floor plans, hardware I/O, local Ktor server, and Room SQLite storage. |
| **Dinevique Kitchen** | Native Android Kitchen Display Board (KDB) | `monitor` | Info Blue (`#2563EB`) | Kitchen-facing display that receives `TICKET_RECEIVED` streams, tracks prep time SLAs, and emits `TICKET_BUMPED` events. |
| **Dinevique Handheld** | Native Android Mobile Waitstaff App | `smartphone` | Pending Ochre (`#B8860B`) | 5-to-7-inch mobile UI (e.g., Sunmi V2) for tableside ordering, seat assignment, and line-busting over local Wi-Fi. |
| **Dinevique Online** | Next.js (SSR/React) Web Storefront | `globe` | Success Green (`#2D7D46`) | Consumer-facing digital storefront for direct online ordering and live order status tracking. |
| **Dinevique Cloud** | Next.js Merchant Admin & Node.js API | `cloud` | Muted Mid (`#6B7280`) | Centralized administration portal for hierarchical menus, pricing overrides, store settings, and multi-location analytics. |

## 2. Target Audience & Operator Personas

| Target Segment | Operational Profile | Core Pain Point Addressed | Primary Dinevique Modules Used |
| :--- | :--- | :--- | :--- |
| **Independent Restaurants & Cafés** | 1–2 terminals, counter or casual table service | Tool fragmentation and fragile internet connectivity | Dinevique POS, Dinevique Kitchen, Dinevique Cloud |
| **Quick-Service & Fast-Casual (QSR)** | High peak throughput, queue bottlenecks, rapid modifiers | Slow order entry and dropped tickets during busy rushes | Dinevique POS, Dinevique Handheld, Dinevique Online, Dinevique Kitchen |
| **Full-Service Dining Establishments** | Multi-course table service, split checks, bar tabs | Order errors between floor and kitchen; slow table turns | Dinevique POS, Dinevique Handheld, Dinevique Kitchen |
| **Multi-Location Restaurant Groups** | 3–25+ venues seeking centralized oversight | Inconsistent menu pricing and delayed cross-store reporting | All 5 Dinevique Modules + Enterprise Cloud Hierarchy |

## 3. System Architecture & Technical Specifications

### 3.1 High-Level Architectural Topology (Master Terminal Edge Topology)
Dinevique eliminates external bridging PCs by utilizing a **100% Native Android Master Terminal Edge Topology** paired with a **Next.js, Node.js, and MongoDB Cloud Tier**.

```
┌────────────────────────────────────────────────────────────────────────────┐
│                           DINEVIQUE CLOUD TIER                             │
│                                                                            │
│  ┌──────────────────────────────────┐  ┌────────────────────────────────┐  │
│  │ Dinevique Online & Cloud Admin   │  │ Self-Hosted MongoDB / Atlas    │  │
│  │ (Next.js SSR / React)            │  │ (Master Menus, Orders, Users)  │  │
│  └────────────────▲─────────────────┘  └───────────────▲────────────────┘  │
│                   │ HTTPS / REST / WSS                 │ Mongoose ODM      │
│                   ▼                                    ▼                   │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │              Cloud Services & API Gateway (Node.js / Express)        │  │
│  └──────────────────────────────────▲───────────────────────────────────┘  │
└─────────────────────────────────────┼──────────────────────────────────────┘
                                      │ Encrypted WSS Tunnel
                                      │ (Idempotent UUID Sync)
══════════════════════════════════════╪═══════════════════════════════════════
       Restaurant Premise — Local Subnet (LAN WebSockets over Wi-Fi/Ethernet)
                                      │
┌─────────────────────────────────────▼──────────────────────────────────────┐
│              MASTER POS TERMINAL — DINEVIQUE POS (Native Android)          │
│                                                                            │
│  ┌───────────────────────────────┐      ┌───────────────────────────────┐  │
│  │ Cashier App UI                │      │ Hardware I/O Engine           │  │
│  │ (Kotlin + Jetpack Compose)    │      │ (UsbManager / Epson / Star)   │  │
│  └───────────────▲───────────────┘      └───────────────┬───────────────┘  │
│                  │ Kotlin Flows                         │ USB / Serial / BT│
│                  ▼                                      ▼                  │
│  ┌───────────────────────────────┐      ┌───────────────────────────────┐  │
│  │ Embedded LAN Server           │◄────►│ Android Room SQLite DB        │  │
│  │ (Ktor HTTP & WebSockets)      │      │ (Menu Cache & Offline Queues) │  │
│  └───────▲───────────────▲───────┘      └───────────────────────────────┘  │
└──────────┼───────────────┼─────────────────────────────────────────────────┘
           │               │
           │ LAN WSS       │ LAN WSS
           ▼               ▼
┌─────────────────────┐ ┌─────────────────────┐    ┌─────────────────────────┐
│ Dinevique Kitchen   │ │ Dinevique Handheld  │    │ Hardware Peripherals    │
│ Native Android KDB  │ │ Native Android POS  │    │ (Thermal/Impact Printers│
│ (Ticket Display)    │ │ (5–7" Sunmi V2 Sled)│    │  & Cash Drawers)        │
└─────────────────────┘ └─────────────────────┘    └─────────────────────────┘
```

### 3.2 Edge Tier Specifications (Native Android Ecosystem)
1. **Dinevique POS — Master POS Terminal (The Orchestrator):**
   * **Runtime & UI:** Native Kotlin application built with **Jetpack Compose** for reactive UI updates tied directly to **Kotlin Flows** observing local database state.
   * **Embedded Server (`Ktor`):** Runs a lightweight HTTP and WebSocket server inside a persistent **Android Foreground Service**. Binds to a static or mDNS-discovered local IP address, acting as the local nerve center for all in-store devices.
   * **Edge Database (`Android Room` over SQLite):** Provides compile-time verified queries, stores the synchronized hierarchical menu tree, caches team PIN credentials, and maintains two critical local tables:
     * `outbound_event_queue`: First-In-First-Out (FIFO) queue for outbound cloud events.
     * `offline_payment_queue`: Encrypted store-and-forward queue for offline card authorizations.
   * **Hardware Abstraction (`UsbManager`):** Uses Android native USB Host APIs, Serial, and Bluetooth alongside manufacturer SDKs (**Epson ePOS**, **StarPRNT**) to communicate directly with thermal receipt printers, impact kitchen printers, and cash drawers without network latency.
2. **Dinevique Kitchen — Kitchen Display Board (KDB):**
   * **Client App:** Native Android application mounted on large kitchen touchscreens or controlled via bump bars.
   * **Network Role:** Maintains a persistent local WebSocket connection to the Master Terminal's Ktor server over the LAN.
   * **Event Contract:** Listens for `TICKET_RECEIVED` events to render incoming orders and emits `TICKET_BUMPED` events back to the Master Terminal when cooks complete items or tickets.
3. **Dinevique Handheld — Mobile Waitstaff POS:**
   * **Client App:** Scaled-down native Android UI optimized for 5-inch to 7-inch screens with integrated payment sleds (e.g., **Sunmi V2**).
   * **Network Role:** Connects via local Wi-Fi exclusively to the Master Terminal (`ORDER_CREATE` / `ORDER_CREATED`). It never communicates directly with the cloud or printers, delegating all transactional persistence, printing, and cloud forwarding to the Master Terminal.
4. **Alternative Cross-Platform Edge Option (Secondary Evaluation Path):**
   * For deployments requiring iPad support alongside Android, the edge tier can optionally deploy **React Native (Expo)** clients for POS, KDB, and Handhelds communicating over LAN WebSockets with a dedicated **Node.js + Express + SQLite Local Broker**. Native Android with embedded Ktor remains the primary reference architecture to avoid external broker hardware.

### 3.3 Cloud Tier Specifications (Next.js, Node.js & MongoDB)
1. **Dinevique Online & Dinevique Cloud (`Next.js`):**
   * Server-Side Rendered (SSR) React web application serving both the public consumer ordering storefront (`Dinevique Online`) and the merchant back-office portal (`Dinevique Cloud`) for menu building, pricing overrides, store settings, and financial analytics.
2. **API Gateway & Event Hub (`Node.js` / `Express`):**
   * Authenticates Master Terminals, processes online payments, ingests third-party delivery webhooks, and maintains persistent encrypted outbound WebSocket tunnels (`WSS`) to edge Master Terminals across all tenant locations.
3. **Cloud Database (`MongoDB` & `Mongoose ODM`):**
   * Document-based NoSQL store (Self-hosted MongoDB or MongoDB Atlas) optimized for deep, hierarchical JSON menu structures (`Menus → Menu Groups → Items → Modifier Groups → Modifier Options → Nested Modifiers`), canonical transaction ledgers, and operator profiles.

## 4. End-to-End Operational Data Workflows

### 4.1 Workflow 1: In-Store Order (Standard Operation)
1. A server enters an order tableside on **Dinevique Handheld** (or at the counter on **Dinevique POS**).
2. The Handheld emits a structured JSON `ORDER_CREATE` payload over the local Wi-Fi WebSocket to the Master POS Terminal's Ktor server.
3. The Master POS writes the order to its **Android Room SQLite** database to guarantee local state persistence.
4. The Master POS triggers the local kitchen impact printer via `UsbManager`/LAN and immediately broadcasts a `TICKET_RECEIVED` event over the LAN WebSocket to **Dinevique Kitchen (KDB)**.
5. Concurrently and asynchronously, the Master POS forwards the payload over its encrypted WAN WebSocket tunnel (`WSS`) to the **Node.js Cloud Gateway** for persistence in **MongoDB**.

### 4.2 Workflow 2: Web Storefront Order (Omnichannel Flow)
1. A customer submits and pays for an order on the **Dinevique Online** Next.js storefront.
2. The **Node.js Cloud Gateway** verifies the payment, writes the canonical order document to **MongoDB**, and pushes the order payload down the persistent `WSS` tunnel to the restaurant's **Master POS Terminal**.
3. The Master POS logs the order in **Android Room SQLite**, triggers an audio chime on the front-of-house tablets to alert the team, and fires `TICKET_RECEIVED` to **Dinevique Kitchen (KDB)**.
4. As the cook bumps the ticket state (`NEW TICKET → IN PROGRESS → READY`) via `TICKET_BUMPED` on the KDB, the Master POS relays the state transition back up the `WSS` tunnel to the Cloud Gateway, updating the customer's live Next.js web tracker in real time.

### 4.3 Workflow 3: WAN Outage & Store-and-Forward Recovery
1. **WAN Interruption:** The outward `WSS` tunnel between the Master POS Terminal and the Cloud Gateway drops. The restaurant's local subnet (LAN) remains active.
2. **Local Continuity:** The embedded **Ktor** server continues routing WebSocket traffic between **Dinevique Handheld**, **Dinevique Kitchen (KDB)**, and the **Room** database. Receipt and kitchen impact printers connected via USB/LAN print without interruption.
3. **Store-and-Forward Queueing:** All state-mutating events are appended chronologically to `outbound_event_queue` in Room SQLite. Swiped, dipped, or tapped credit card payloads are encrypted and stored in `offline_payment_queue`.
4. **WAN Restoration & Queue Draining:** When the Master Terminal detects internet restoration, it establishes a fresh `WSS` tunnel to the Cloud Gateway and drains `outbound_event_queue` and `offline_payment_queue` chronologically in batches.
5. **Idempotent Cloud Ingestion:** Every order, ticket, and payment event carries a client-generated `UUID` created at the edge. If a payload is sent twice during an unstable reconnection, the Node.js Cloud Gateway executes an idempotent MongoDB upsert (`$set` matched on `clientEventUuid`), preventing duplicate kitchen tickets or double-charging guests.

## 5. Core Functional Requirements & Epics (Prioritized)

Prioritization uses MoSCoW definitions:
* **P0 (MVP Must-Have):** Required for v1.0 pilot deployment across counter, table, kitchen, and online workflows.
* **P1 (Commercial v1.0 Target):** Required for full multi-terminal scale and advanced service workflows.

### 5.1 Epic 1: DineviquePOS (Master Terminal & Front-of-House)

| ID | Priority | Functional Requirement | Acceptance Criteria (Given / When / Then) |
| :--- | :--- | :--- | :--- |
| **POS-1** | **P0** | **Embedded Ktor Orchestrator & Peer Discovery:** Master Terminal runs a persistent Android Foreground Service hosting the local Ktor HTTP/WSS server and mDNS broadcast. | **Given** the Master Terminal boots on the restaurant Wi-Fi, **When** a Dinevique Handheld or KDB opens on the same subnet, **Then** it discovers and connects to the Ktor WebSocket within 2 seconds without manual IP entry. |
| **POS-2** | **P0** | **Fast Team PIN Switching:** Team members authenticate via a 4–6 digit PIN pad with role-based permissions (*Server*, *Cashier*, *Cook*, *Manager*). | **Given** a locked POS screen, **When** a team member enters a valid 4-digit PIN, **Then** their active tables and order workspace render via Jetpack Compose in under 300ms. |
| **POS-3** | **P0** | **Table Floor Plan, Seat Ordering & Coursing:** Interactive table map displaying occupancy state, guest count, and elapsed time; supports seat-level item assignment and course holds (`Hold` vs. `Fire`). | **Given** Table 7 has appetizers set to `Fire` and mains set to `Hold`, **When** the order is sent, **Then** only appetizers dispatch `TICKET_RECEIVED` to the KDB until the server or expo triggers "Fire Mains". |
| **POS-4** | **P0** | **Split Checks & Split Tenders:** Support splitting checks by seat, moving items between checks, splitting checks evenly across N ways, and partial tender payments. | **Given** a $90 check across 3 seats, **When** the server selects "Split by Seat", **Then** 3 balanced sub-checks are created in Room SQLite and synced to the Ktor broker with exact tax rounding. |
| **POS-5** | **P0** | **Direct USB/Serial Hardware Abstraction:** Native `UsbManager`, Epson ePOS, and StarPRNT integration for thermal receipt printers, impact kitchen printers, and cash drawers. | **Given** a cash payment is recorded on the Master Terminal, **When** the transaction commits to Room, **Then** the RJ12 cash drawer kicks open in < 100ms and the thermal receipt prints without cloud round-trip. |
| **POS-6** | **P0** | **Store-and-Forward Offline Payments:** Encrypt and queue card transactions in `offline_payment_queue` during WAN outages with configurable single-transaction and cumulative risk limits. | **Given** the WAN tunnel is down, **When** a guest pays $48 via card (under the $150 offline cap), **Then** the encrypted payload saves to `offline_payment_queue`, the check closes, and the transaction settles automatically upon WAN recovery. |

### 5.2 Epic 2: Dinevique Kitchen (Kitchen Display Board — KDB)

| ID | Priority | Functional Requirement | Acceptance Criteria (Given / When / Then) |
| :--- | :--- | :--- | :--- |
| **KDB-1** | **P0** | **Real-Time WebSocket Ticket Stream:** Render incoming `TICKET_RECEIVED` payloads immediately on dark-mode high-contrast cards (`#0D0D0D` canvas, `#1E2A38` standard card frame, `#D4820A` active frame). | **Given** an order is fired on a Handheld or Online Storefront, **When** the Master Terminal processes `ORDER_CREATE`, **Then** the KDB displays the ticket in under 400ms with an optional audio alert. |
| **KDB-2** | **P0** | **Ticket State Transitions & Bump Workflow:** Support transitioning tickets from `NEW TICKET` (`#2D7D46` pill) to `IN PROGRESS` (`#D4820A` pill) to `BUMPED` via touch or hardware bump bar. | **Given** Ticket #4421 is active on Dinevique Kitchen, **When** the cook taps `BUMP ->`, **Then** the card flashes Brand Amber (`#D4820A`) for 100ms, dismisses, and emits `TICKET_BUMPED` to the Master Terminal. |
| **KDB-3** | **P1** | **Station Filtering, Aging Timers & Recall:** Filter KDB view by prep station (*Grill*, *Fry*, *Salad*, *Pastry*, *Expo*), display elapsed cook timers, and recall bumped tickets within 60 minutes. | **Given** a cook accidentally bumps Ticket #4422, **When** they open the "Recall" drawer and tap Ticket #4422, **Then** the ticket restores to its active position on the KDB and broadcasts its reopened state. |
| **KDB-4** | **P1** | **Live 86ing from the Pass:** Allow kitchen leads or managers to mark an item out-of-stock or set a remaining portion countdown directly from the KDB or Master POS. | **Given** the kitchen marks "Risotto" as 86ed on the KDB, **When** the Master Terminal receives the event, **Then** Risotto is disabled across all Handhelds, POS terminals, and the Next.js `Dinevique Online` storefront within 1 second. |

### 5.3 Epic 3: Dinevique Handheld (Mobile Waitstaff POS)

| ID | Priority | Functional Requirement | Acceptance Criteria (Given / When / Then) |
| :--- | :--- | :--- | :--- |
| **HND-1** | **P0** | **Compact 5–7" Ergonomic UI:** Responsive native Android interface tailored for Sunmi V2 and handheld payment sleds with thumb-friendly navigation and zero local printer dependencies. | **Given** a server is at Table 7 with a Sunmi V2 handheld, **When** they select items and required modifiers, **Then** they can fire the order to the Master Terminal in 3 taps or fewer. |
| **HND-2** | **P0** | **Delegated Master Terminal Sync:** All handheld reads and writes proxy through the Master Terminal's Ktor WebSocket server; handhelds never write directly to MongoDB or printers. | **Given** the restaurant WAN internet is disconnected, **When** a server enters an order on Dinevique Handheld over local Wi-Fi, **Then** the Master Terminal accepts `ORDER_CREATE`, prints the kitchen ticket, and updates the KDB normally. |
| **HND-3** | **P1** | **Tableside Tap-to-Pay & Receipt Dispatch:** Accept EMV/NFC tap payments on the handheld sled, capture on-screen tips, and trigger paper receipts at the counter or digital email/SMS receipts. | **Given** a guest taps their card on the handheld at Table 7, **When** payment is approved, **Then** the check marks `PAID` on the Master Terminal floor plan immediately. |

### 5.4 Epic 4: Dinevique Online (Next.js Consumer Web Storefront)

| ID | Priority | Functional Requirement | Acceptance Criteria (Given / When / Then) |
| :--- | :--- | :--- | :--- |
| **ONL-1** | **P0** | **SSR Digital Menu & Modifier Drawer:** Server-Side Rendered Next.js storefront that dynamically renders the restaurant's active MongoDB menu hierarchy, modifier rules, and 86ed availability. | **Given** an item has required modifier groups in MongoDB, **When** a guest views the item on `Dinevique Online`, **Then** the "Add to Order" CTA remains disabled until required selections are satisfied. |
| **ONL-2** | **P0** | **Direct Checkout & Edge Tunnel Push:** Process online card payments via the Node.js Gateway, persist the order in MongoDB, and push it down the `WSS` tunnel to the Master Terminal. | **Given** a guest completes checkout online, **When** payment succeeds, **Then** the order chimes on `Dinevique POS` and renders on `Dinevique Kitchen` within 2 seconds without manual tablet re-entry. |
| **ONL-3** | **P0** | **Live Order Status Tracker:** Real-time consumer web view that updates (`Received → In Progress → Ready for Pickup`) as the kitchen bumps the ticket on the KDB. | **Given** a customer is viewing their order confirmation page, **When** the cook taps `BUMP ->` on the restaurant KDB, **Then** the web page status transitions to `Ready for Pickup` via WebSocket/SSE. |

### 5.5 Epic 5: Dinevique Cloud (Next.js Admin Portal & MongoDB Schema)

| ID | Priority | Functional Requirement | Acceptance Criteria (Given / When / Then) |
| :--- | :--- | :--- | :--- |
| **CLD-1** | **P0** | **Deep JSON Menu & Modifier Studio:** Next.js merchant interface for creating and editing nested MongoDB menu documents (`Menus → Groups → Items → Modifier Groups → Options`), prep stations, and tax rates. | **Given** an operator updates the price of "Steak" in `Dinevique Cloud`, **When** they click "Publish", **Then** the Node.js Gateway pushes a `MENU_SYNC` event down the `WSS` tunnel, updating Android Room on the Master Terminal. |
| **CLD-2** | **P0** | **Idempotent Event Ingestion Engine:** Node.js/Express gateway endpoint and WebSocket handler that deduplicates replayed `outbound_event_queue` batches using client-generated UUIDs. | **Given** a Master Terminal replays a batch of 50 queued offline orders with existing `clientEventUuid` values, **When** ingested by the Cloud Gateway, **Then** MongoDB executes `$set` upserts resulting in 0 duplicate orders or double charges. |
| **CLD-3** | **P0** | **Live Sales, Tender & Shift Analytics:** Cloud dashboard displaying real-time gross/net sales, tax collected, tip totals, payment method breakdown, and WAN connection status per location. | **Given** an operator logs into `Dinevique Cloud`, **When** viewing the dashboard, **Then** they see live synced revenue totals, active terminal heartbeat status, and historical transaction logs. |

## 6. MongoDB & Android Room Data Models

### 6.1 Canonical Menu Document Schema (MongoDB / Mongoose ODM)

```json
{
  "_id": "menu_6713a9f0e4b0a1c2d3e4f5a6",
  "restaurantId": "rest_9812b4c1d2e3f4a5b6c7d8e9",
  "name": "All-Day Dining Menu",
  "isActive": true,
  "updatedAt": "2026-10-19T12:00:00Z",
  "categories": [
    {
      "categoryId": "cat_mains",
      "name": "Mains",
      "displayOrder": 1,
      "items": [
        {
          "itemId": "item_steak_01",
          "name": "Steak",
          "basePriceCents": 3400,
          "taxRatePercent": 8.875,
          "prepStation": "GRILL",
          "isAvailable": true,
          "modifierGroups": [
            {
              "groupId": "modgrp_temp",
              "name": "Temperature",
              "minSelection": 1,
              "maxSelection": 1,
              "isRequired": true,
              "options": [
                { "optionId": "opt_mr", "name": "Med-Rare", "priceDeltaCents": 0 },
                { "optionId": "opt_med", "name": "Medium", "priceDeltaCents": 0 }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

### 6.2 Edge Room SQLite Entities (`Dinevique POS` Master Terminal)

```sql
-- 1. Local Order State Table (Android Room)
CREATE TABLE local_orders (
    client_order_uuid TEXT PRIMARY KEY NOT NULL,
    restaurant_id TEXT NOT NULL,
    table_label TEXT,
    source_channel TEXT NOT NULL, -- 'POS', 'HANDHELD', 'ONLINE'
    ticket_status TEXT NOT NULL,  -- 'NEW_TICKET', 'IN_PROGRESS', 'READY', 'PAID'
    order_payload_json TEXT NOT NULL,
    subtotal_cents INTEGER NOT NULL,
    tax_cents INTEGER NOT NULL,
    tip_cents INTEGER NOT NULL DEFAULT 0,
    total_cents INTEGER NOT NULL,
    created_at_epoch_ms INTEGER NOT NULL,
    updated_at_epoch_ms INTEGER NOT NULL
);

-- 2. Outbound Cloud Sync Queue (FIFO Store-and-Forward)
CREATE TABLE outbound_event_queue (
    event_uuid TEXT PRIMARY KEY NOT NULL,
    event_type TEXT NOT NULL,     -- 'ORDER_CREATE', 'TICKET_BUMPED', 'ORDER_PAID'
    entity_uuid TEXT NOT NULL,
    payload_json TEXT NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    sync_status TEXT NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'IN_FLIGHT', 'ACKED'
    created_at_epoch_ms INTEGER NOT NULL
);

-- 3. Encrypted Offline Card Authorization Queue
CREATE TABLE offline_payment_queue (
    payment_uuid TEXT PRIMARY KEY NOT NULL,
    client_order_uuid TEXT NOT NULL,
    amount_cents INTEGER NOT NULL,
    tip_cents INTEGER NOT NULL DEFAULT 0,
    encrypted_card_payload TEXT NOT NULL,
    card_last4 TEXT NOT NULL,
    card_brand TEXT NOT NULL,
    captured_at_epoch_ms INTEGER NOT NULL,
    settlement_status TEXT NOT NULL DEFAULT 'QUEUED' -- 'QUEUED', 'SETTLED', 'FAILED'
);
```

## 7. Design System, UI/UX & Brand Compliance Requirements

All five Dinevique modules must strictly enforce the **Dinevique Strategic Brand Guidelines v1.0**:

### 7.1 Color System & WCAG AA Contrast Rules

| Color Token | Hex | RGB | Approved UI Role & Accessibility Guardrail |
| :--- | :--- | :--- | :--- |
| **Brand Onyx** | `#0D0D0D` | `13, 13, 13` | Primary background for `Dinevique Kitchen` (KDB), dark POS mode, and web hero sections. |
| **Brand Slate** | `#1E2A38` | `30, 42, 56` | Secondary dark surface for KDB standard ticket frames, sidebars, and panel fills. |
| **Brand Ivory** | `#F7F5F1` | `247, 245, 241` | Light background for `Dinevique Cloud` content pages and `Dinevique Online` menus. |
| **Brand Amber** | `#D4820A` | `212, 130, 10` | Primary interactive CTA fill, active ticket border, and active icon state. **CRITICAL WCAG RULE:** Never use `#D4820A` as text on `#F7F5F1` or `#FFFFFF` (~3.0:1 FAIL). Always pair `#D4820A` button fills with **Brand Ink (`#1A1A1A`)** text (~6.4:1 PASS). |
| **Brand Ink** | `#1A1A1A` | `26, 26, 26` | Primary readable text on light backgrounds (`#F7F5F1`, `#FFFFFF`, `#E8E4DC`) and on `#D4820A` CTA buttons. |
| **Brand Linen** | `#E8E4DC` | `232, 228, 220` | 1px structural dividers, table borders, and skeleton loading shimmers. |
| **Semantic Status** | `#2D7D46` / `#C0392B` / `#2563EB` / `#B8860B` | — | **Success Green (`#2D7D46`)** for `NEW TICKET` / sync complete; **Alert Red (`#C0392B`)** for WAN interruption / payment error; **Info Blue (`#2563EB`)** for Kitchen module chip; **Pending Ochre (`#B8860B`)** for queued states. |

### 7.2 Typography, Iconography & Motion Standards
* **Typography:**
  * **Inter** (weights 300–800) for all product interfaces across Android and Next.js. Minimum body font size is `14px` (`12px` absolute floor for captions/labels). All UI text must be left-aligned (never justified).
  * **Playfair Display** (weights 400, 700) restricted strictly to `Dinevique Online` / marketing hero headlines and the italic tagline *The Art of Smarter Dining.*
* **Iconography:**
  * **Lucide** (or Phosphor) outlined icons with `2px` stroke weight and `2–3px` rounded corners. Minimum `32px` size on KDB displays. Emoji are strictly prohibited in all UI and formal documentation.
* **Motion ("Clarity at Speed"):**
  * UI must respond within `100ms` of touch interaction.
  * Screen transitions use `cubic-bezier(0.4, 0, 0.2, 1)` at `250–300ms`; micro-interactions at `100–120ms`.
  * **KDB Ticket Bump Animation:** Tapping `BUMP ->` triggers a `100ms` flash of Brand Amber (`#D4820A`) before settling or dismissing.
  * All web and native animations must honor `prefers-reduced-motion`.
* **UI Copy & Tone Guardrails:**
  * Use imperative, active verbs on buttons (`Print Ticket`, `Send Order`, `Bump ->`).
  * Exclamation marks (`!`) are prohibited across all interfaces, notifications, and error modals.
  * Error messages must state what happened, why, and the exact recovery action (e.g., *"WAN connection interrupted. Orders and card payments are queued locally on the Master Terminal and will synchronize automatically when connectivity returns."*).

### 7.3 Reference Interface Wireframe: Dinevique Kitchen (KDB)

```
+------------------------------------------------------------------+
|  BG: #0D0D0D  |  DINEVIQUE KITCHEN  |  Table 7  |  2 tickets     |
+----------------+-------------------------------------------------+
|  TICKET #4421  |  TICKET #4422                                   |
|  [#D4820A      |  [#1E2A38 frame -- standard]                    |
|   frame active]|                                                 |
|                |                                                 |
|  - Pasta x2    |  - Steak (Med-Rare)                             |
|  - Risotto x1  |  - Side Salad x2                                |
|                |  - Tiramisu x1                                  |
|  IN PROGRESS   |  NEW TICKET                                     |
|  [#D4820A pill]|  [#2D7D46 pill]                                 |
|                |                                                 |
|  [ BUMP -> ]   |  [ BUMP -> ]                                    |
|  [#D4820A btn] |  [#D4820A btn]                                  |
+----------------+-------------------------------------------------+
|  [DINEVIQUE icon mark -- white, 20px, bottom-left]               |
+------------------------------------------------------------------+
```

## 8. Non-Functional Requirements, Compliance & Release Governance

### 8.1 Performance & Reliability SLAs

| Metric | Target Benchmark | Architectural Enforcement |
| :--- | :--- | :--- |
| **UI Touch-to-Feedback Latency** | `< 100ms` | Jetpack Compose reactive state bound directly to local Android Room Flows. |
| **Handheld-to-KDB Ticket Propagation** | `< 400ms` over LAN | Direct LAN WebSocket routing via the Master Terminal's embedded Ktor server. |
| **USB Receipt & Kitchen Print Trigger** | `< 150ms` | Direct Android `UsbManager` / Epson ePOS / StarPRNT hardware calls. |
| **WAN Outage Failover Disruption** | `0 dropped orders` | Automatic fallback to Room `outbound_event_queue` and `offline_payment_queue`. |
| **Idempotent Cloud Replay Accuracy** | `100% deduplication` | Client-generated UUIDs reconciled via MongoDB `$set` upserts on the Node.js Gateway. |

### 8.2 Pre-Commercialization Gateways & Legal Clearances
In accordance with the Dinevique Company Profile and Brand Guidelines, the following gates must be cleared prior to production release:
1. **Payment & Offline Card Certification:** Offline card encryption, tokenization, risk ceiling controls, and EMV/NFC sled integrations (e.g., Sunmi V2) must complete payment processor certification and PCI-DSS validation before production deployment.
2. **Hardware Compatibility Matrix:** Publish a verified hardware compatibility list (HCL) covering tested Android Master Terminal tablets, Sunmi handhelds, and Epson/Star thermal and impact printer models.
3. **Brand & Domain Clearance:** Complete legal trademark registration and domain clearance for **Dinevique** (`dinevique.com`) prior to public storefront launch.
