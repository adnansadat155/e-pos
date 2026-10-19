# Canonical User Stories: Dinevique Platform

**Document ID:** `USER_STORIES`  
**Parent:** [SRS_INDEX.md](SRS_INDEX.md)  
**Source Baseline:** [SYSTEM_USER_STORIES.md](../SOURCES/SYSTEM_USER_STORIES.md)  
**Cross-References:** [REQUIREMENTS_TRACEABILITY_MATRIX.md](REQUIREMENTS_TRACEABILITY_MATRIX.md)  
**Version:** 1.0.0  
**Status:** Approved  

---

## Table of Contents
- [1. User Personas](#1-user-personas)
- [2. Epic 1: Dinevique POS (Master Terminal & Front-of-House)](#2-epic-1-dinevique-pos)
- [3. Epic 2: Dinevique Kitchen (Kitchen Display Board - KDB)](#3-epic-2-dinevique-kitchen)
- [4. Epic 3: Dinevique Handheld (Mobile Waitstaff POS)](#4-epic-3-dinevique-handheld)
- [5. Epic 4: Dinevique Online (Consumer Web Storefront)](#5-epic-4-dinevique-online)
- [6. Epic 5: Dinevique Cloud (Merchant Admin & Central Gateway)](#6-epic-5-dinevique-cloud)
- [7. Story Point Estimation & Complexity](#7-story-point-estimation--complexity)

---

## 1. User Personas

- **PER-01: Elena (Floor Server)** — Quick order taking, mobile floor maps, seat coursing, split checks.
- **PER-02: Marcus (Head Bartender / Cashier)** — High-volume counter sales, instant PIN entry, cash drawer kicks, thermal receipts.
- **PER-03: Chef Mateo (Kitchen Lead / Expo)** — High-contrast ticket streams, prep station filtering, ticket bump micro-interactions, pass 86ing.
- **PER-04: Sarah (General Manager)** — Shift reconciliation, offline payment risk management, void audits.
- **PER-05: Liam & Maya (Online Diners)** — Frictionless mobile menu browsing, modifier drawers, real-time live prep tracker.
- **PER-06: Vikram (Multi-Unit Franchise Owner)** — Multi-location menu publishing, idempotent sales sync, real-time revenue analytics.

---

## 2. Epic 1: Dinevique POS (Master Terminal & Front-of-House)

### Story US-POS-001: Automatic Master LAN Discovery & Ktor Service Initialization
- **ID:** `US-POS-001` | **Points:** 5 | **Priority:** P0
- **Traces:** PRD: `POS-1` | SRS: `FR-POS-001` | Contract: `EVENT-SYS-DISCOVERY` | Spec: `SPEC-POS-MASTER` | Test: `TC-POS-001`
- **User Narrative:** As Marcus (Cashier), I want the Master Terminal to automatically host a local service and broadcast its presence on the local network so that all kitchen boards and handheld tablets connect immediately without manual IP configuration.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Cold Boot Discovery
    Given the Master Terminal boots on the restaurant Wi-Fi
    When Dinevique POS app launches
    Then a foreground service starts Ktor HTTP/WSS on port 8080
    And mDNS registers '_dinevique-pos._tcp.local.'
    And auxiliary Handheld and KDB devices discover the Master within 2 seconds.

  Scenario: Wi-Fi Interface Flap
    Given the in-store Wi-Fi router reassigns local DHCP IP addresses
    When network connectivity reconnects
    Then the Master Terminal re-binds Ktor to the new local IP
    And broadcasts the updated mDNS advertisement within 3 seconds.
  ```

---

### Story US-POS-002: Sub-300ms Team PIN Authentication & Rapid Lock
- **ID:** `US-POS-002` | **Points:** 3 | **Priority:** P0
- **Traces:** PRD: `POS-2` | SRS: `FR-POS-002` | Contract: `SCHEMA-USER-SESSION` | Spec: `SPEC-POS-MASTER` | Test: `TC-POS-002`
- **User Narrative:** As Elena (Server), I want to authenticate instantly with my 4-digit PIN and have the terminal lock quickly after inactivity so that service is fast and guest checks remain secure.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Valid Fast PIN Entry
    Given the POS terminal displays the locked PIN screen
    When Elena enters her 4-digit PIN
    Then the PIN validates against the local Room SQLite hash in under 50ms
    And her active tables and check workspace render in under 300ms.

  Scenario: Inactivity Auto-Lock
    Given a team member is logged in
    When 60 seconds elapse with zero touch interaction
    Then the terminal screen immediately locks to the PIN pad, preserving state.
  ```

---

### Story US-POS-003: Table Floor Plan, Seat Assignment & Course Holds
- **ID:** `US-POS-003` | **Points:** 8 | **Priority:** P0
- **Traces:** PRD: `POS-3` | SRS: `FR-POS-003` | Contract: `EVENT-ORDER-CREATE` | Spec: `SPEC-POS-MASTER` | Test: `TC-POS-003`
- **User Narrative:** As Elena (Server), I want to assign items to specific seats and hold main courses so that food arrives in timed sequence without kitchen miscommunication.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Firing Appetizers while Holding Mains
    Given Table 7 has 2 appetizers set to 'Fire' and 2 steaks set to 'Hold'
    When Elena taps 'Send Order'
    Then only the 2 appetizers dispatch to Dinevique Kitchen (KDB)
    And the 2 steaks remain in 'HELD' status on the order workspace.

  Scenario: Firing Held Course
    Given Table 7 has finished appetizers
    When Elena taps 'Fire Mains'
    Then a COURSE_FIRED event emits to KDB in under 200ms.
  ```

---

### Story US-POS-004: Split Checks by Seat and Even N-Ways with Penny-Perfect Rounding
- **ID:** `US-POS-004` | **Points:** 8 | **Priority:** P0
- **Traces:** PRD: `POS-4` | SRS: `FR-POS-004` | Contract: `SCHEMA-ORDER` | Spec: `SPEC-POS-MASTER` | Test: `TC-POS-004`
- **User Narrative:** As Marcus (Cashier), I want to split a dining check by seat or evenly across multiple cards so that guests can pay separately without math errors.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Split by Seat
    Given an open check with items assigned to Seat 1 ($42.00) and Seat 2 ($38.00)
    When Marcus taps 'Split by Seat'
    Then two separate sub-checks are generated in Room SQLite with exact seat items and proportional tax.

  Scenario: Even N-Way Split with Penny Balancing
    Given a total check of $100.00 split 3 ways
    When Marcus tenders payments
    Then checks are divided into $33.34, $33.33, and $33.33, totaling exactly $100.00.
  ```

---

### Story US-POS-005: Direct Hardware Control of Printers and Cash Drawers
- **ID:** `US-POS-005` | **Points:** 5 | **Priority:** P0
- **Traces:** PRD: `POS-5` | SRS: `FR-POS-005` | Contract: `CONTRACT-HARDWARE-IO` | Spec: `SPEC-POS-HARDWARE` | Test: `TC-POS-005`
- **User Narrative:** As Marcus (Cashier), I want the thermal receipt printer to cut and the cash drawer to kick open instantly when cash is tendered so that customers are not kept waiting.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Cash Checkout Drawer Kick
    Given Marcus records a cash payment on the Master Terminal
    When the transaction commits to Room SQLite
    Then the RJ12 cash drawer kicks open in under 100ms
    And the thermal receipt prints within 150ms without cloud latency.
  ```

---

### Story US-POS-006: Store-and-Forward Offline Payment Authorizations
- **ID:** `US-POS-006` | **Points:** 8 | **Priority:** P0
- **Traces:** PRD: `POS-6` | SRS: `FR-POS-006` | Contract: `SCHEMA-OFFLINE-PAYMENT` | Spec: `SPEC-POS-MASTER` | Test: `TC-POS-006`
- **User Narrative:** As Sarah (General Manager), I want the system to safely accept and encrypt card payments during an internet outage up to our preconfigured risk ceiling so that revenue is protected and guests are never turned away.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Offline Card Payment Under Ceiling
    Given the restaurant WAN connection is down
    When Marcus swipes a card for a $48.50 transaction
    Then the card data is encrypted with AES-256 GCM in offline_payment_queue
    And the check is marked 'PAID (OFFLINE)' and closed locally.

  Scenario: Offline Cumulative Ceiling Exceeded
    Given offline queued payments reach the $2,500 restaurant limit
    When an offline transaction of $80 is attempted
    Then the POS blocks the offline charge and prompts for cash or manager override.
  ```

---

## 3. Epic 2: Dinevique Kitchen (Kitchen Display Board - KDB)

### Story US-KDB-001: Sub-400ms Ticket Stream & High-Contrast Visual Alerts
- **ID:** `US-KDB-001` | **Points:** 5 | **Priority:** P0
- **Traces:** PRD: `KDB-1` | SRS: `FR-KDB-001` | Contract: `EVENT-TICKET-RECEIVED` | Spec: `SPEC-KITCHEN-KDB` | Test: `TC-KDB-001`
- **User Narrative:** As Chef Mateo (Expo), I want new kitchen tickets to appear on the KDB within 400ms of being fired so that our cooks can start preparing dishes immediately.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Incoming Ticket Arrival
    Given Chef Mateo is viewing the Brand Onyx (#0D0D0D) KDB screen
    When an order is fired on Handheld or POS
    Then the ticket card renders on KDB in under 400ms with a Brand Slate frame and Success Green badge
    And an optional audio chime alerts the kitchen line.
  ```

---

### Story US-KDB-002: Ticket Progression & Micro-Interaction Bump Workflow
- **ID:** `US-KDB-002` | **Points:** 5 | **Priority:** P0
- **Traces:** PRD: `KDB-2` | SRS: `FR-KDB-002` | Contract: `EVENT-TICKET-BUMPED` | Spec: `SPEC-KITCHEN-KDB` | Test: `TC-KDB-002`
- **User Narrative:** As Chef Mateo (Line Cook), I want to tap or bump a ticket from New to In Progress to Completed so that the front-of-house and expo know exact dish status.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Bumping Ticket to In-Progress
    Given Ticket #104 is in 'NEW TICKET' state
    When Mateo taps the card header
    Then the card border turns Brand Amber (#D4820A) and status updates to 'IN PROGRESS'.

  Scenario: Bumping Completed Ticket
    Given Ticket #104 preparation is complete
    When Mateo taps 'BUMP ->'
    Then the card flashes Brand Amber for 100ms and dismisses
    And dispatches TICKET_BUMPED over LAN WebSocket to the Master Terminal.
  ```

---

### Story US-KDB-003: Station-Specific Filtering & 60-Minute Ticket Recall
- **ID:** `US-KDB-003` | **Points:** 5 | **Priority:** P1
- **Traces:** PRD: `KDB-3` | SRS: `FR-KDB-003` | Contract: `EVENT-TICKET-RECALLED` | Spec: `SPEC-KITCHEN-KDB` | Test: `TC-KDB-003`
- **User Narrative:** As a grill cook, I want to filter my screen to show only grill items and recall tickets if bumped by mistake so that my workspace is focused and error-free.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Station Filtering
    Given the KDB terminal is assigned to the 'GRILL' station
    When an order with 1 Steak (Grill) and 1 Salad (Cold Station) arrives
    Then only the Steak displays on this KDB terminal.

  Scenario: Recalling Bumped Ticket
    Given a cook accidentally bumped Ticket #104 within the last 60 minutes
    When they open the Recall drawer and tap Ticket #104
    Then the ticket restores to its active position on the board.
  ```

---

### Story US-KDB-004: Rapid 86ing from the Pass with Global Propagation
- **ID:** `US-KDB-004` | **Points:** 5 | **Priority:** P1
- **Traces:** PRD: `KDB-4` | SRS: `FR-KDB-004` | Contract: `EVENT-ITEM-86` | Spec: `SPEC-KITCHEN-KDB` | Test: `TC-KDB-004`
- **User Narrative:** As Chef Mateo (Head Chef), I want to 86 an ingredient or dish directly on the KDB screen so that servers and online customers cannot order it within seconds.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: 86ing an Item
    Given the kitchen runs out of 'Sea Bass'
    When Mateo marks 'Sea Bass' 86ed on the KDB
    Then an ITEM_86ED event broadcasts to the Master Terminal
    And Sea Bass shows disabled on all Handhelds, POS terminals, and online storefronts within 1 second.
  ```

---

## 4. Epic 3: Dinevique Handheld (Mobile Waitstaff POS)

### Story US-HND-001: 3-Tap Mobile Order Entry on Sunmi V2 Hardware
- **ID:** `US-HND-001` | **Points:** 5 | **Priority:** P0
- **Traces:** PRD: `HND-1` | SRS: `FR-HND-001` | Contract: `EVENT-ORDER-CREATE` | Spec: `SPEC-HANDHELD-MOBILE` | Test: `TC-HND-001`
- **User Narrative:** As Elena (Server), I want to select dishes and required modifiers on a compact 5-to-7-inch mobile screen in 3 taps or fewer so that I can maintain eye contact with guests while ordering tableside.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: 3-Tap Item and Modifier Selection
    Given Elena is at Table 7 using a Sunmi V2
    When she taps 'Salmon Fillet' (Tap 1), selects 'Wild Rice' (Tap 2), and taps 'Send' (Tap 3)
    Then the order sends to the Master Terminal in under 150ms.
  ```

---

### Story US-HND-002: Delegated LAN Master Terminal Synchronization
- **ID:** `US-HND-002` | **Points:** 5 | **Priority:** P0
- **Traces:** PRD: `HND-2` | SRS: `FR-HND-002` | Contract: `CONTRACT-LAN-WEBSOCKET` | Spec: `SPEC-HANDHELD-MOBILE` | Test: `TC-HND-002`
- **User Narrative:** As Elena (Server), I want my handheld to communicate strictly through the Master Terminal so that tableside ordering continues smoothly during internet blackouts.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Offline LAN Ordering
    Given the restaurant internet connection drops
    When Elena sends Table 9's order from her handheld over in-store Wi-Fi
    Then the Master Terminal receives ORDER_CREATE, persists to Room, and prints kitchen tickets normally.
  ```

---

### Story US-HND-003: Tableside Tap-to-Pay and Digital Receipt Delivery
- **ID:** `US-HND-003` | **Points:** 8 | **Priority:** P1
- **Traces:** PRD: `HND-3` | SRS: `FR-HND-003` | Contract: `SCHEMA-PAYMENT-RECORD` | Spec: `SPEC-HANDHELD-MOBILE` | Test: `TC-HND-003`
- **User Narrative:** As a guest, I want to tap my card at the table and choose whether to receive a text/email receipt or paper receipt so that checkout is fast and paperless.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Tableside Tap & Tip
    Given Elena presents the handheld at Table 5
    When the guest selects 20% tip and taps their contactless card
    Then payment approves, Table 5 marks 'PAID' on the POS floor plan, and digital receipt is sent.
  ```

---

## 5. Epic 4: Dinevique Online (Consumer Web Storefront)

### Story US-ONL-001: Dynamic SSR Menu Rendering with Strict Modifier Rules
- **ID:** `US-ONL-001` | **Points:** 5 | **Priority:** P0
- **Traces:** PRD: `ONL-1` | SRS: `FR-ONL-001` | Contract: `SCHEMA-MENU-CANONICAL` | Spec: `SPEC-ONLINE-STOREFRONT` | Test: `TC-ONL-001`
- **User Narrative:** As Liam (Online Guest), I want to browse an accurate, fast-loading digital menu that prevents me from ordering without selecting required options so that my takeout order is prepared correctly.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Required Modifiers Guard
    Given Liam views the 'Wagyu Burger' with mandatory Temperature selection
    When no temperature option has been chosen
    Then the 'Add to Order' CTA remains disabled
    And selecting 'Medium' enables the button immediately.
  ```

---

### Story US-ONL-002: Web Checkout and Direct WSS Edge Tunnel Push
- **ID:** `US-ONL-002` | **Points:** 8 | **Priority:** P0
- **Traces:** PRD: `ONL-2` | SRS: `FR-ONL-002` | Contract: `EVENT-ORDER-CLOUD-INGEST` | Spec: `SPEC-ONLINE-STOREFRONT` | Test: `TC-ONL-002`
- **User Narrative:** As Liam (Online Guest), I want my paid web order to transmit directly to the restaurant kitchen so that staff do not have to manually re-enter orders from a third-party tablet.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Online Order Ingestion Downlink
    Given Liam completes checkout on dinevique.com
    When payment authorization succeeds
    Then Node.js Cloud Gateway routes the order down the WSS tunnel to the store's Master Terminal
    And the Master Terminal chimes and displays the ticket on Dinevique Kitchen within 2 seconds.
  ```

---

### Story US-ONL-003: Real-Time Live Order Prep Tracker
- **ID:** `US-ONL-003` | **Points:** 5 | **Priority:** P0
- **Traces:** PRD: `ONL-3` | SRS: `FR-ONL-003` | Contract: `EVENT-ORDER-STATUS-UPDATE` | Spec: `SPEC-ONLINE-STOREFRONT` | Test: `TC-ONL-003`
- **User Narrative:** As Maya (Online Guest), I want to watch my order status update live on my smartphone browser as the kitchen prepares it so that I know exactly when to arrive for pickup.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Live Order Tracker Update
    Given Maya is viewing her order tracker web page
    When the cook taps 'BUMP ->' on the in-store KDB
    Then the tracker transitions from 'In the Kitchen' to 'Ready for Pickup' in under 1 second without page refresh.
  ```

---

## 6. Epic 5: Dinevique Cloud (Merchant Admin & Central Gateway)

### Story US-CLD-001: Centralized Hierarchical Menu Studio & Instant Edge Sync
- **ID:** `US-CLD-001` | **Points:** 8 | **Priority:** P0
- **Traces:** PRD: `CLD-1` | SRS: `FR-CLD-001` | Contract: `EVENT-MENU-SYNC` | Spec: `SPEC-CLOUD-ADMIN` | Test: `TC-CLD-001`
- **User Narrative:** As Vikram (Franchise Owner), I want to edit menu prices and categories in the cloud admin and publish them down to all store terminals simultaneously so that pricing is consistent.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Global Menu Publishing
    Given Vikram updates menu item prices in Dinevique Cloud
    When he clicks 'Publish'
    Then a MENU_SYNC event broadcasts to all store Master Terminals
    And local Room databases update within 2 seconds.
  ```

---

### Story US-CLD-002: Idempotent Cloud Event Ingestion Engine
- **ID:** `US-CLD-002` | **Points:** 8 | **Priority:** P0
- **Traces:** PRD: `CLD-2` | SRS: `FR-CLD-002` | Contract: `CONTRACT-IDEMPOTENCY` | Spec: `SPEC-CLOUD-GATEWAY` | Test: `TC-CLD-002`
- **User Narrative:** As Vikram (Franchise Owner), I want all replayed offline orders to be ingested idempotently by the cloud gateway so that duplicate network transmissions never result in duplicate orders or double-charged guests.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Duplicate Ingestion Deduplication
    Given a Master Terminal replays 50 queued orders with clientEventUuid tokens
    When network retries cause the batch to be transmitted twice
    Then Node.js Gateway executes MongoDB $set upserts
    And exactly 50 records exist in MongoDB with 0 duplicates.
  ```

---

### Story US-CLD-003: Real-Time Sales, Tender & Terminal Heartbeat Analytics
- **ID:** `US-CLD-003` | **Points:** 5 | **Priority:** P0
- **Traces:** PRD: `CLD-3` | SRS: `FR-CLD-003` | Contract: `API-ANALYTICS-SUMMARY` | Spec: `SPEC-CLOUD-ADMIN` | Test: `TC-CLD-003`
- **User Narrative:** As Vikram (Franchise Owner), I want to view live store revenue, payment method breakdowns, and terminal connection health on a single dashboard so that I have instant visibility into operations.
- **Acceptance Criteria (Gherkin):**
  ```gherkin
  Scenario: Live Dashboard Aggregates
    Given Vikram opens Dinevique Cloud
    When viewing the Store Health page
    Then live sales update within 3 seconds of in-store settlement
    And terminals display green heartbeat indicators if pinged in the last 30 seconds.
  ```

---

## 7. Story Point Estimation & Complexity

| Epic | Story Count | Total Story Points | Target Milestone |
| :--- | :--- | :--- | :--- |
| **Epic 1: Dinevique POS** | 6 stories | 37 points | Phase 01 / Phase 02 / Phase 04 |
| **Epic 2: Dinevique Kitchen** | 4 stories | 20 points | Phase 01 / Phase 02 |
| **Epic 3: Dinevique Handheld** | 3 stories | 18 points | Phase 02 / Phase 04 |
| **Epic 4: Dinevique Online** | 3 stories | 18 points | Phase 03 |
| **Epic 5: Dinevique Cloud** | 3 stories | 21 points | Phase 01 / Phase 03 |
| **Total Platform MVP** | **19 stories** | **114 points** | **Phases 01–05** |

---
_Document: USER_STORIES.md | Node: REQUIREMENTS | Parent: [SRS_INDEX.md](SRS_INDEX.md)_
