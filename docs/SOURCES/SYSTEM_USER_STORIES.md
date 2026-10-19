# System User Stories: Dinevique Platform

**Version:** 1.0.0  
**Parent Document:** [SYSTEM_PRD.md](SYSTEM_PRD.md)  
**Cross-References:** [SYSTEM_SRS.md](SYSTEM_SRS.md), [SYSTEM_TRACEABILITY_AND_TEST_CASES.md](SYSTEM_TRACEABILITY_AND_TEST_CASES.md), [USER_STORIES.md](../REQUIREMENTS/USER_STORIES.md)  
**Master Brand:** Dinevique — *The Art of Smarter Dining.*  
**Date:** 2026-10-19  
**Status:** Approved Baseline  

---

## Table of Contents

- [1. User Personas & Roles](#1-user-personas--roles)
- [2. Epic 1: Dinevique POS (Master Terminal & Front-of-House)](#2-epic-1-dinevique-pos)
- [3. Epic 2: Dinevique Kitchen (Kitchen Display Board - KDB)](#3-epic-2-dinevique-kitchen)
- [4. Epic 3: Dinevique Handheld (Mobile Waitstaff POS)](#4-epic-3-dinevique-handheld)
- [5. Epic 4: Dinevique Online (Consumer Web Storefront)](#5-epic-4-dinevique-online)
- [6. Epic 5: Dinevique Cloud (Merchant Admin & Central Gateway)](#6-epic-5-dinevique-cloud)
- [7. User Story Cross-Reference Summary](#7-user-story-cross-reference-summary)

---

## 1. User Personas & Roles

| Persona ID | Role Name | System Interface | Primary Objective | Key Frustrations |
| :--- | :--- | :--- | :--- | :--- |
| **PER-01** | **Elena (Floor Server)** | Dinevique Handheld & POS | Rapid table turnaround, accurate order entry, effortless seat coursing and split billing | Clunky modifier flows, dropped tickets, slow payment readers |
| **PER-02** | **Marcus (Head Bartender / Cashier)** | Dinevique POS (Master) | Lightning-fast counter checkout, quick PIN switching, instant cash drawer opening, receipt printing | Laggy touch interfaces, delayed drawer kicks, complex split checks |
| **PER-03** | **Chef Mateo (Kitchen Lead / Expo)** | Dinevique Kitchen (KDB) | Clear visual ticket prioritization, accurate course timing, zero missed modifications, 1-tap 86ing | Illegible paper tickets, kitchen printer jams, lost ticket history |
| **PER-04** | **Sarah (General Manager)** | Dinevique POS & Cloud | Shift management, void authorizations, offline revenue continuity, real-time labor & sales auditing | System freezes during peak rush, mismatched ledger counts during outages |
| **PER-05** | **Liam & Maya (Online Diners)** | Dinevique Online (Web) | Frictionless takeout/delivery ordering, allergen customization, transparent live order status | Delayed order confirmation, inability to customize items, static trackers |
| **PER-06** | **Vikram (Multi-Unit Franchise Owner)** | Dinevique Cloud (Admin) | Centralized menu updating, unified sales reporting across locations, dependable cloud synchronization | Menu discrepancies across stores, duplicate charges from network retries |

---

## 2. Epic 1: Dinevique POS (Master Terminal & Front-of-House)

### Story US-POS-001: Automatic Master LAN Discovery & Ktor Service Initialization
- **Role:** System / Cashier (Marcus)
- **Statement:** As a cashier, I want the Master Terminal to automatically host a local service and broadcast its presence on the local network so that all kitchen boards and handheld tablets connect immediately without manual IP configuration.
- **Traceability:** PRD §5.1 (POS-1) | SRS §4.1 (FR-POS-001) | Spec: `SPEC-POS-MASTER.md` | Contract: `EVENT-SYS-DISCOVERY.md` | Test: `TC-POS-001`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Cold Boot Discovery**
    - **Given** the Master Terminal Android tablet powers on and establishes a LAN connection (Wi-Fi or Ethernet),
    - **When** the Dinevique POS app launches,
    - **Then** a background foreground service initiates a Ktor HTTP/WSS server listening on port 8080 and registers an mDNS service `_dinevique-pos._tcp.local.`,
    - **And** auxiliary devices on the same subnet discover and connect to the Master WebSocket in under 2 seconds.
  - **Scenario 2: Network Subnet Flap**
    - **Given** the in-store Wi-Fi router reboots and reassigns local DHCP IP addresses,
    - **When** connectivity is re-established,
    - **Then** the Master Terminal re-binds Ktor to the new local interface IP and announces the new address via mDNS within 3 seconds.

---

### Story US-POS-002: Sub-300ms Team PIN Authentication & Rapid Lock
- **Role:** Floor Server (Elena) / Cashier (Marcus)
- **Statement:** As a floor server, I want to authenticate instantly with my 4-digit PIN and have the terminal lock quickly after inactivity so that service is fast and guest checks remain secure.
- **Traceability:** PRD §5.1 (POS-2) | SRS §4.1 (FR-POS-002) | Spec: `SPEC-POS-MASTER.md` | Contract: `SCHEMA-USER-SESSION.md` | Test: `TC-POS-002`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Valid PIN Entry**
    - **Given** the POS terminal displays the locked Brand Onyx PIN screen,
    - **When** Elena enters her 4-digit PIN `4821`,
    - **Then** the PIN is validated against the salted Room SQLite hash in under 50ms,
    - **And** the active floor plan and Elena's assigned open tables render via Jetpack Compose in under 300ms.
  - **Scenario 2: Auto-Lock on Idle**
    - **Given** Marcus is logged in on the Master POS,
    - **When** no touch interactions occur for 60 seconds,
    - **Then** the screen immediately blanks back to the secure PIN pad view, preserving the open check in memory.

---

### Story US-POS-003: Table Floor Plan, Seat Assignment & Course Holds
- **Role:** Floor Server (Elena)
- **Statement:** As a floor server, I want to assign items to specific seats and hold main courses so that food arrives in timed sequence without kitchen miscommunication.
- **Traceability:** PRD §5.1 (POS-3) | SRS §4.1 (FR-POS-003) | Spec: `SPEC-POS-MASTER.md` | Contract: `EVENT-ORDER-CREATE.md` | Test: `TC-POS-003`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Firing Appetizers while Holding Mains**
    - **Given** Table 12 has 2 guests seated with 2 appetizers tagged `Fire` and 2 steaks tagged `Hold`,
    - **When** Elena taps "Send Order",
    - **Then** an `ORDER_CREATE` transaction commits to Room SQLite,
    - **And** only the 2 appetizers appear on Dinevique Kitchen (KDB) with ticket status `NEW TICKET`,
    - **And** the 2 steaks remain in status `HELD` visible on the server's check workspace.
  - **Scenario 2: Firing Held Course**
    - **Given** Table 12 has finished appetizers,
    - **When** Elena selects Table 12 and taps "Fire Mains",
    - **Then** a `COURSE_FIRED` event dispatches over the local WebSocket to the KDB within 200ms.

---

### Story US-POS-004: Split Checks by Seat and Even N-Ways with Penny-Perfect Rounding
- **Role:** Cashier (Marcus) / Server (Elena)
- **Statement:** As a server, I want to split a dining check by seat or evenly across multiple cards so that guests can pay separately without math errors.
- **Traceability:** PRD §5.1 (POS-4) | SRS §4.1 (FR-POS-004) | Spec: `SPEC-POS-MASTER.md` | Contract: `SCHEMA-ORDER.md` | Test: `TC-POS-004`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Split by Seat**
    - **Given** an open check with items assigned to Seat 1 ($42.00) and Seat 2 ($38.00),
    - **When** Elena taps "Split by Seat",
    - **Then** two sub-checks are generated in Room SQLite with exact seat-level items and proportional sales tax applied.
  - **Scenario 2: Split Evenly Across 3 Tenders with Odd Cent**
    - **Given** a total check of $100.00 split 3 ways,
    - **When** Marcus splits the tender,
    - **Then** Tender 1 is $33.34, Tender 2 is $33.33, and Tender 3 is $33.33, summing exactly to $100.00.

---

### Story US-POS-005: Direct Hardware Control of Printers and Cash Drawers
- **Role:** Cashier (Marcus)
- **Statement:** As a cashier, I want the thermal receipt printer to cut and the cash drawer to kick open instantly when cash is tendered so that customers are not kept waiting.
- **Traceability:** PRD §5.1 (POS-5) | SRS §4.1 (FR-POS-005) | Spec: `SPEC-POS-HARDWARE.md` | Contract: `CONTRACT-HARDWARE-IO.md` | Test: `TC-POS-005`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Cash Checkout Kick**
    - **Given** Marcus tenders a $50 cash payment on a $35 check,
    - **When** he taps "Tender Cash",
    - **Then** the Master Terminal emits the RJ12 kick-pulse via USB Host API in under 100ms,
    - **And** the thermal printer prints the itemized receipt with exact change due ($15.00) within 150ms without cloud latency.

---

### Story US-POS-006: Store-and-Forward Offline Payment Authorizations
- **Role:** General Manager (Sarah) / Cashier (Marcus)
- **Statement:** As a restaurant manager, I want the system to safely accept and encrypt card payments during an internet outage up to our preconfigured risk ceiling so that revenue is protected and guests are never turned away.
- **Traceability:** PRD §5.1 (POS-6) | SRS §4.1 (FR-POS-006) | Spec: `SPEC-POS-MASTER.md` | Contract: `SCHEMA-OFFLINE-PAYMENT.md` | Test: `TC-POS-006`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Offline Card Payment Under Ceiling**
    - **Given** the restaurant's WAN connection is down,
    - **When** Marcus swipes a guest's card for a $48.50 check (under the $150 transaction limit),
    - **Then** the card data is encrypted using AES-256 GCM and stored in `offline_payment_queue`,
    - **And** the check is marked `PAID (OFFLINE)` and closed locally.
  - **Scenario 2: Offline Risk Ceiling Exceeded**
    - **Given** cumulative offline queued payments have reached the $2,500 restaurant ceiling,
    - **When** Marcus attempts an offline card transaction of $75.00,
    - **Then** the POS displays a clear alert stating the offline processing ceiling has been reached and requests a manager override or cash tender.

---

## 3. Epic 2: Dinevique Kitchen (Kitchen Display Board - KDB)

### Story US-KDB-001: Sub-400ms Ticket Stream & High-Contrast Visual Alerts
- **Role:** Head Chef / Expo (Chef Mateo)
- **Statement:** As an expo chef, I want new kitchen tickets to appear on the KDB within 400ms of being fired so that our cooks can start preparing dishes immediately.
- **Traceability:** PRD §5.2 (KDB-1) | SRS §4.2 (FR-KDB-001) | Spec: `SPEC-KITCHEN-KDB.md` | Contract: `EVENT-TICKET-RECEIVED.md` | Test: `TC-KDB-001`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Real-Time Ticket Arrival**
    - **Given** Chef Mateo is monitoring the KDB on Brand Onyx (`#0D0D0D`) display,
    - **When** Elena fires Table 4 on her handheld,
    - **Then** the ticket card renders on the KDB within 400ms with a Brand Slate (`#1E2A38`) frame and `NEW TICKET` Success Green (`#2D7D46`) pill,
    - **And** an optional short acoustic chime sounds through the tablet speaker.

---

### Story US-KDB-002: Ticket Progression & Micro-Interaction Bump Workflow
- **Role:** Line Cook / Expo (Chef Mateo)
- **Statement:** As a line cook, I want to tap or bump a ticket from New to In Progress to Completed so that the front-of-house and expo know exact dish status.
- **Traceability:** PRD §5.2 (KDB-2) | SRS §4.2 (FR-KDB-002) | Spec: `SPEC-KITCHEN-KDB.md` | Contract: `EVENT-TICKET-BUMPED.md` | Test: `TC-KDB-002`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Bumping to In-Progress**
    - **Given** Ticket #104 is in status `NEW TICKET`,
    - **When** Chef Mateo taps the ticket header,
    - **Then** the border highlights in Brand Amber (`#D4820A`) and status switches to `IN PROGRESS`.
  - **Scenario 2: Final Ticket Bump Dismissal**
    - **Given** Ticket #104 has all items plated,
    - **When** Mateo taps `BUMP ->`,
    - **Then** the card flashes Brand Amber for 100ms, animates out of view, and dispatches a `TICKET_BUMPED` event to the Master Terminal over LAN WebSocket.

---

### Story US-KDB-003: Station-Specific Filtering & 60-Minute Ticket Recall
- **Role:** Line Cook (Grill Cook) / Expo (Chef Mateo)
- **Statement:** As a grill cook, I want to filter my screen to show only grill items and recall tickets if bumped by mistake so that my workspace is focused and error-free.
- **Traceability:** PRD §5.2 (KDB-3) | SRS §4.2 (FR-KDB-003) | Spec: `SPEC-KITCHEN-KDB.md` | Contract: `EVENT-TICKET-RECALLED.md` | Test: `TC-KDB-003`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Station Filtering**
    - **Given** the KDB is toggled to station `GRILL`,
    - **When** a multi-station order arrives containing 1 Steak and 2 Caesar Salads,
    - **Then** only the Steak item renders on the grill display.
  - **Scenario 2: Ticket Recall within 60 Minutes**
    - **Given** Mateo accidentally bumped Ticket #104 3 minutes ago,
    - **When** he opens the "Recall" drawer and taps Ticket #104,
    - **Then** Ticket #104 reappears in its exact position on the active board and notifies the Master Terminal.

---

### Story US-KDB-004: Rapid 86ing from the Pass with Global Propagation
- **Role:** Head Chef (Chef Mateo)
- **Statement:** As head chef, I want to 86 an ingredient or dish directly on the KDB screen so that servers and online customers cannot order it within seconds.
- **Traceability:** PRD §5.2 (KDB-4) | SRS §4.2 (FR-KDB-004) | Spec: `SPEC-KITCHEN-KDB.md` | Contract: `EVENT-ITEM-86.md` | Test: `TC-KDB-004`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: 86ing Dish from Kitchen**
    - **Given** the kitchen runs out of "Wild Salmon",
    - **When** Mateo taps "86 Item" and selects "Wild Salmon",
    - **Then** an `ITEM_86ED` event transmits to the Master Terminal via LAN WebSocket,
    - **And** within 1 second, "Wild Salmon" shows disabled/greyed-out across all Dinevique Handhelds, Dinevique POS terminals, and Dinevique Online storefronts.

---

## 4. Epic 3: Dinevique Handheld (Mobile Waitstaff POS)

### Story US-HND-001: 3-Tap Mobile Order Entry on Sunmi V2 Hardware
- **Role:** Floor Server (Elena)
- **Statement:** As a floor server, I want to select dishes and required modifiers on a compact 5-to-7-inch mobile screen in 3 taps or fewer so that I can maintain eye contact with guests while ordering tableside.
- **Traceability:** PRD §5.3 (HND-1) | SRS §4.3 (FR-HND-001) | Spec: `SPEC-HANDHELD-MOBILE.md` | Contract: `EVENT-ORDER-CREATE.md` | Test: `TC-HND-001`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Efficient Modifier Flow**
    - **Given** Elena is at Table 7 using a Sunmi V2 handheld,
    - **When** she taps "Ribeye Steak" (Tap 1), selects "Medium-Rare" from the bottom sheet (Tap 2), and taps "Send" (Tap 3),
    - **Then** the order payload dispatches to the Master Terminal over Wi-Fi in under 150ms.

---

### Story US-HND-002: Delegated LAN Master Terminal Synchronization
- **Role:** Floor Server (Elena)
- **Statement:** As a floor server, I want my handheld to communicate strictly through the Master Terminal so that tableside ordering continues smoothly during internet blackouts.
- **Traceability:** PRD §5.3 (HND-2) | SRS §4.3 (FR-HND-002) | Spec: `SPEC-HANDHELD-MOBILE.md` | Contract: `CONTRACT-LAN-WEBSOCKET.md` | Test: `TC-HND-002`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Tableside Ordering During Internet Blackout**
    - **Given** the external restaurant internet connection drops,
    - **When** Elena sends an order for Table 9 from her handheld over local Wi-Fi,
    - **Then** the Master Terminal receives the payload, saves it to local Room SQLite, prints the kitchen ticket, and updates the KDB without error.

---

### Story US-HND-003: Tableside Tap-to-Pay and Digital Receipt Delivery
- **Role:** Floor Server (Elena) / Guest
- **Statement:** As a guest, I want to tap my card at the table and choose whether to receive a text/email receipt or paper receipt so that checkout is fast and paperless.
- **Traceability:** PRD §5.3 (HND-3) | SRS §4.3 (FR-HND-003) | Spec: `SPEC-HANDHELD-MOBILE.md` | Contract: `SCHEMA-PAYMENT-RECORD.md` | Test: `TC-HND-003`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Tableside Tap & Tip**
    - **Given** Elena brings the Sunmi V2 to Table 5 for checkout,
    - **When** the guest selects an 18% tip and taps their contactless card,
    - **Then** the transaction authorizes, the check closes, and Table 5 switches to `PAID` on the floor plan map.
  - **Scenario 2: Digital Receipt Routing**
    - **Given** payment authorization completes,
    - **When** the guest inputs their mobile phone number for an SMS receipt,
    - **Then** the Master Terminal queues the receipt payload for cloud dispatch.

---

## 5. Epic 4: Dinevique Online (Consumer Web Storefront)

### Story US-ONL-001: Dynamic SSR Menu Rendering with Strict Modifier Rules
- **Role:** Online Diner (Liam & Maya)
- **Statement:** As an online guest, I want to browse an accurate, fast-loading digital menu that prevents me from ordering without selecting required options so that my takeout order is prepared correctly.
- **Traceability:** PRD §5.4 (ONL-1) | SRS §4.4 (FR-ONL-001) | Spec: `SPEC-ONLINE-STOREFRONT.md` | Contract: `SCHEMA-MENU-CANONICAL.md` | Test: `TC-ONL-001`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Mandatory Modifier Guard**
    - **Given** Liam views the "Gourmet Burger" requiring a Bun selection (min: 1, max: 1),
    - **When** Liam has not selected a bun option,
    - **Then** the "Add to Order" button remains disabled with Brand Ink text,
    - **And** selecting "Brioche" enables the button instantly.

---

### Story US-ONL-002: Web Checkout and Direct WSS Edge Tunnel Push
- **Role:** Online Diner (Liam) / Cashier (Marcus)
- **Statement:** As an online customer, I want my paid web order to transmit directly to the restaurant kitchen so that staff do not have to manually re-enter orders from a third-party tablet.
- **Traceability:** PRD §5.4 (ONL-2) | SRS §4.4 (FR-ONL-002) | Spec: `SPEC-ONLINE-STOREFRONT.md` | Contract: `EVENT-ORDER-CLOUD-INGEST.md` | Test: `TC-ONL-002`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Direct Cloud-to-Edge Order Push**
    - **Given** Liam completes checkout on `dinevique.com` for pickup,
    - **When** payment succeeds,
    - **Then** the Node.js Cloud Gateway routes the order down the active WSS tunnel to the store's Master Terminal in under 2 seconds,
    - **And** the Master Terminal chimes, commits to Room SQLite, and dispatches the ticket to Dinevique Kitchen (KDB).

---

### Story US-ONL-003: Real-Time Live Order Prep Tracker
- **Role:** Online Diner (Maya)
- **Statement:** As an online customer, I want to watch my order status update live on my smartphone browser as the kitchen prepares it so that I know exactly when to arrive for pickup.
- **Traceability:** PRD §5.4 (ONL-3) | SRS §4.4 (FR-ONL-003) | Spec: `SPEC-ONLINE-STOREFRONT.md` | Contract: `EVENT-ORDER-STATUS-UPDATE.md` | Test: `TC-ONL-003`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Live Status Transition**
    - **Given** Maya is viewing her order confirmation page,
    - **When** the restaurant line cook bumps her ticket on the in-store KDB,
    - **Then** the status bar on Maya's phone transitions from `In Kitchen` to `Ready for Pickup` via SSE/WebSocket within 1 second without page refresh.

---

## 6. Epic 5: Dinevique Cloud (Merchant Admin & Central Gateway)

### Story US-CLD-001: Centralized Hierarchical Menu Studio & Instant Edge Sync
- **Role:** Franchise Owner (Vikram) / General Manager (Sarah)
- **Statement:** As an operator, I want to edit menu prices and categories in the cloud admin and publish them down to all store terminals simultaneously so that pricing is consistent.
- **Traceability:** PRD §5.5 (CLD-1) | SRS §4.5 (FR-CLD-001) | Spec: `SPEC-CLOUD-ADMIN.md` | Contract: `EVENT-MENU-SYNC.md` | Test: `TC-CLD-001`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Price Update and Global Sync**
    - **Given** Vikram updates the price of "Espresso" from $3.50 to $3.75 in Dinevique Cloud,
    - **When** he clicks "Publish Changes",
    - **Then** a `MENU_SYNC` event broadcasts down the WSS tunnel to all registered Master Terminals,
    - **And** Android Room updates within 2 seconds, reflecting the new price across all in-store POS terminals and Handhelds.

---

### Story US-CLD-002: Idempotent Cloud Event Ingestion Engine
- **Role:** System / Franchise Owner (Vikram)
- **Statement:** As an operator, I want all replayed offline orders to be ingested idempotently by the cloud gateway so that duplicate network transmissions never result in duplicate orders or double-charged guests.
- **Traceability:** PRD §5.5 (CLD-2) | SRS §4.5 (FR-CLD-002) | Spec: `SPEC-CLOUD-GATEWAY.md` | Contract: `CONTRACT-IDEMPOTENCY.md` | Test: `TC-CLD-002`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Deduplication of Replayed Offline Batch**
    - **Given** a Master Terminal reconnects to WAN and replays 100 offline events,
    - **When** network fluctuations cause the batch to be transmitted twice,
    - **Then** the Node.js Cloud Gateway matches `clientEventUuid` and applies MongoDB `$set` upserts,
    - **And** exactly 100 orders exist in MongoDB with 0 duplicate revenue entries.

---

### Story US-CLD-003: Real-Time Sales, Tender & Terminal Heartbeat Analytics
- **Role:** Franchise Owner (Vikram) / General Manager (Sarah)
- **Statement:** As an owner, I want to view live store revenue, payment method breakdowns, and terminal connection health on a single dashboard so that I have instant visibility into operations.
- **Traceability:** PRD §5.5 (CLD-3) | SRS §4.5 (FR-CLD-003) | Spec: `SPEC-CLOUD-ADMIN.md` | Contract: `API-ANALYTICS-SUMMARY.md` | Test: `TC-CLD-003`
- **Acceptance Criteria (Gherkin):**
  - **Scenario 1: Real-Time Heartbeat Status**
    - **Given** Vikram opens Dinevique Cloud on his laptop,
    - **When** viewing the "Locations" page,
    - **Then** each restaurant displays a green indicator if its Master Terminal responded to heartbeat ping within the last 30 seconds, or an amber/red warning if disconnected.

---

## 7. User Story Cross-Reference Summary

| Story ID | Epic | Primary Persona | PRD Ref | SRS Ref | Contract Ref | Test Case Ref |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **US-POS-001** | Epic 1 | Cashier / System | POS-1 | FR-POS-001 | `EVENT-SYS-DISCOVERY` | `TC-POS-001` |
| **US-POS-002** | Epic 1 | Server / Cashier | POS-2 | FR-POS-002 | `SCHEMA-USER-SESSION` | `TC-POS-002` |
| **US-POS-003** | Epic 1 | Server | POS-3 | FR-POS-003 | `EVENT-ORDER-CREATE` | `TC-POS-003` |
| **US-POS-004** | Epic 1 | Server / Cashier | POS-4 | FR-POS-004 | `SCHEMA-ORDER` | `TC-POS-004` |
| **US-POS-005** | Epic 1 | Cashier | POS-5 | FR-POS-005 | `CONTRACT-HARDWARE-IO` | `TC-POS-005` |
| **US-POS-006** | Epic 1 | Manager / Cashier | POS-6 | FR-POS-006 | `SCHEMA-OFFLINE-PAYMENT`| `TC-POS-006` |
| **US-KDB-001** | Epic 2 | Kitchen Expo | KDB-1 | FR-KDB-001 | `EVENT-TICKET-RECEIVED` | `TC-KDB-001` |
| **US-KDB-002** | Epic 2 | Line Cook | KDB-2 | FR-KDB-002 | `EVENT-TICKET-BUMPED` | `TC-KDB-002` |
| **US-KDB-003** | Epic 2 | Line Cook / Expo | KDB-3 | FR-KDB-003 | `EVENT-TICKET-RECALLED` | `TC-KDB-003` |
| **US-KDB-004** | Epic 2 | Head Chef | KDB-4 | FR-KDB-004 | `EVENT-ITEM-86` | `TC-KDB-004` |
| **US-HND-001** | Epic 3 | Floor Server | HND-1 | FR-HND-001 | `EVENT-ORDER-CREATE` | `TC-HND-001` |
| **US-HND-002** | Epic 3 | Floor Server | HND-2 | FR-HND-002 | `CONTRACT-LAN-WEBSOCKET`| `TC-HND-002` |
| **US-HND-003** | Epic 3 | Server / Guest | HND-3 | FR-HND-003 | `SCHEMA-PAYMENT-RECORD` | `TC-HND-003` |
| **US-ONL-001** | Epic 4 | Online Diner | ONL-1 | FR-ONL-001 | `SCHEMA-MENU-CANONICAL` | `TC-ONL-001` |
| **US-ONL-002** | Epic 4 | Online Diner / POS | ONL-2 | FR-ONL-002 | `EVENT-ORDER-CLOUD-INGEST`| `TC-ONL-002` |
| **US-ONL-003** | Epic 4 | Online Diner | ONL-3 | FR-ONL-003 | `EVENT-ORDER-STATUS-UPDATE`| `TC-ONL-003` |
| **US-CLD-001** | Epic 5 | Owner / GM | CLD-1 | FR-CLD-001 | `EVENT-MENU-SYNC` | `TC-CLD-001` |
| **US-CLD-002** | Epic 5 | System / Operator | CLD-2 | FR-CLD-002 | `CONTRACT-IDEMPOTENCY` | `TC-CLD-002` |
| **US-CLD-003** | Epic 5 | Owner / GM | CLD-3 | FR-CLD-003 | `API-ANALYTICS-SUMMARY` | `TC-CLD-003` |
