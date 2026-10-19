# System Software Requirements Specification (SRS): Dinevique Platform

**Version:** 1.0.0  
**Parent Document:** [SYSTEM_PRD.md](SYSTEM_PRD.md)  
**Cross-References:** [SYSTEM_USER_STORIES.md](SYSTEM_USER_STORIES.md), [SYSTEM_TRACEABILITY_AND_TEST_CASES.md](SYSTEM_TRACEABILITY_AND_TEST_CASES.md), [SRS_INDEX.md](../REQUIREMENTS/SRS_INDEX.md)  
**Master Brand:** Dinevique — *The Art of Smarter Dining.*  
**Date:** 2026-10-19  
**Status:** Approved Baseline  

---

## Table of Contents

- [1. Introduction](#1-introduction)
  - [1.1 Purpose & Scope](#11-purpose--scope)
  - [1.2 System Overview & Architectural Paradigm](#12-system-overview--architectural-paradigm)
  - [1.3 Definitions, Acronyms & Abbreviations](#13-definitions-acronyms--abbreviations)
- [2. Overall Description](#2-overall-description)
  - [2.1 Product Perspective & System Context](#21-product-perspective--system-context)
  - [2.2 User Classes & Operational Personas](#22-user-classes--operational-personas)
  - [2.3 Operating Environment](#23-operating-environment)
  - [2.4 Design & Implementation Constraints](#24-design--implementation-constraints)
  - [2.5 Assumptions & Dependencies](#25-assumptions--dependencies)
- [3. External Interface Requirements](#3-external-interface-requirements)
  - [3.1 User Interfaces (Jetpack Compose & Next.js SSR)](#31-user-interfaces)
  - [3.2 Hardware Interfaces (USB, RJ12, Thermal/Impact Printers)](#32-hardware-interfaces)
  - [3.3 Software Interfaces (Android Room SQLite & Cloud MongoDB)](#33-software-interfaces)
  - [3.4 Communications Interfaces (LAN WebSockets, Ktor, WSS Cloud Tunnel)](#34-communications-interfaces)
- [4. System Features & Detailed Functional Requirements](#4-system-features--detailed-functional-requirements)
  - [4.1 Epic 1: Dinevique POS (Master Terminal & Orchestrator)](#41-epic-1-dinevique-pos)
  - [4.2 Epic 2: Dinevique Kitchen (Kitchen Display Board - KDB)](#42-epic-2-dinevique-kitchen)
  - [4.3 Epic 3: Dinevique Handheld (Mobile Waitstaff POS)](#43-epic-3-dinevique-handheld)
  - [4.4 Epic 4: Dinevique Online (Consumer Web Storefront)](#44-epic-4-dinevique-online)
  - [4.5 Epic 5: Dinevique Cloud (Merchant Admin & Central Gateway)](#45-epic-5-dinevique-cloud)
- [5. Non-Functional Requirements](#5-non-functional-requirements)
  - [5.1 Performance & Latency SLAs](#51-performance--latency-slas)
  - [5.2 Reliability & High Availability (Store-and-Forward)](#52-reliability--high-availability)
  - [5.3 Security, Hashing & PCI-DSS Compliance](#53-security-hashing--pci-dss-compliance)
  - [5.4 Accessibility (WCAG 2.1 AA) & Brand Tone](#54-accessibility--brand-tone)
- [6. Verification & Validation Matrices](#6-verification--validation-matrices)

---

## 1. Introduction

### 1.1 Purpose & Scope
This Software Requirements Specification (SRS) establishes the complete, authoritative technical and behavioral specifications for release **v1.0 MVP** of the **Dinevique Platform**. It translates the high-level objectives in [SYSTEM_PRD.md](SYSTEM_PRD.md) into unambiguous functional statements, input/output data flows, operational constraints, and verification criteria for engineering, QA, and automated agentic execution.

### 1.2 System Overview & Architectural Paradigm
Dinevique employs a hybrid **Native Android Master Terminal Edge Topology** coupled with a **Next.js / Node.js / MongoDB Cloud Tier**.
- **Edge Tier:** Eliminates legacy in-store Windows/PC controller hardware. The primary counter terminal (Dinevique POS) functions as an autonomous local server running an embedded Ktor HTTP/WebSocket engine inside an Android Foreground Service, writing to local Android Room SQLite.
- **LAN Cluster:** Auxiliary in-store terminals—Dinevique Handheld (tableside) and Dinevique Kitchen (KDB)—communicate directly with the Master Terminal over local Wi-Fi/Ethernet WebSockets.
- **Cloud Tier:** Centralizes multi-tenant back-office management, menu authoring, digital consumer ordering (Dinevique Online), and cross-store aggregated analytics, synchronized via persistent bi-directional encrypted WebSocket tunnels (`WSS`).

### 1.3 Definitions, Acronyms & Abbreviations
- **Master Terminal:** The designated primary Android POS tablet hosting Ktor, managing local hardware peripherals, and maintaining the cloud sync tunnel.
- **KDB:** Kitchen Display Board (Dinevique Kitchen).
- **Store-and-Forward:** Resilient data pattern where state mutations are recorded locally in a durable FIFO SQLite table during internet failure and replayed chronologically when connectivity is restored.
- **Idempotency Key (`clientEventUuid`):** A client-generated RFC 4122 v4 UUID attached to every transaction event preventing duplicate execution during network replay.
- **86ing:** Restaurant terminology for marking an inventory item or modifier option as immediately out-of-stock.

---

## 2. Overall Description

### 2.1 Product Perspective & System Context
Dinevique unifies fragmented restaurant operations into five synchronized modules:
1. **Dinevique POS (`receipt` / `#D4820A`):** Master counter terminal orchestrating dining room floor plans, checks, peripherals, and local routing.
2. **Dinevique Kitchen (`monitor` / `#2563EB`):** Real-time KDB rendering active ticket queues, prep SLAs, and bump coordination.
3. **Dinevique Handheld (`smartphone` / `#B8860B`):** Mobile tableside device (Sunmi V2) for ordering and payment.
4. **Dinevique Online (`globe` / `#2D7D46`):** Consumer web portal for responsive digital ordering and real-time prep tracking.
5. **Dinevique Cloud (`cloud` / `#6B7280`):** Merchant back-office portal for hierarchical menu management, employee permissions, and reporting.

### 2.2 User Classes & Operational Personas
- **Cashier / Counter Server:** High transaction speed, quick PIN switching, rapid modifier selection, fast cash/card checkout.
- **Tableside Server:** Mobile floor plan navigation, seat assignment, course hold/fire controls, split billing.
- **Line Cook / Kitchen Expo:** Instant visual ticket alerts, high-contrast dark-mode legibility, station filtering, single-tap ticket bumping.
- **Restaurant Manager:** Live floor supervision, 86ing items, voiding items with audit reason codes, reviewing end-of-shift drawer tallies.
- **Enterprise Owner / Operator:** Multi-store menu distribution, tax rate management, labor and sales aggregate analytics.
- **Dining Guest:** Intuitive mobile-optimized web ordering, transparent allergen/modifier customization, live order tracking.

### 2.3 Operating Environment
- **Master & In-Store Tablets:** Android 11.0 (API Level 30) or higher, 4GB+ RAM, Ethernet or dual-band 802.11ac Wi-Fi.
- **Handheld Sleds:** Sunmi V2 / V2 Pro (Android 7.1/10), integrated 58mm thermal printer (optional), NFC/EMV card reader.
- **Peripherals:** Epson TM-T88VI/VII (Thermal Receipt), Epson TM-U220 (Impact Kitchen), APG Series 100 Cash Drawer (RJ12).
- **Cloud Environment:** Node.js 20 LTS runtime, Next.js 14+ App Router, MongoDB 7.0+ (WiredTiger storage engine).

### 2.4 Design & Implementation Constraints
- **Zero Cloud Runtime Dependency for In-Store Flow:** Core dining, ordering, and kitchen printing must continue uninterrupted during complete WAN outages.
- **Strict Brand Design System:** Implementation must strictly adhere to Brand Onyx (`#0D0D0D`), Brand Amber (`#D4820A`), Brand Slate (`#1E2A38`), Inter typography, and Lucide icons.
- **WCAG AA Compliance:** All interactive elements must maintain at least 4.5:1 text contrast (Brand Amber button fills paired strictly with Brand Ink `#1A1A1A` text) and minimum 48x48dp touch targets.

---

## 3. External Interface Requirements

### 3.1 User Interfaces
- **Android Front-of-House & Handheld:** Jetpack Compose, state-driven rendering from Room Kotlin Flows, zero-jank UI updates (< 16ms frame budget).
- **Kitchen Display Board:** Jetpack Compose 10-foot UI / counter layout, Brand Onyx background, dark-adapted typography, animated bump cards.
- **Consumer Web & Admin:** Next.js SSR with Tailwind CSS / CSS Modules matching Dinevique Brand Tokens, fully responsive from mobile viewport (375px) to 4K displays.

### 3.2 Hardware Interfaces
- **USB Host Engine:** Android `android.hardware.usb.UsbManager` with permission filters for Epson and Star VID/PIDs.
- **Cash Drawer Interface:** Direct kick-pulse via printer escape code sequence (`ESC p m t1 t2` / `\x1B\x70\x00\x19\xFA`) sent through receipt printer RJ12 port.

### 3.3 Software Interfaces
- **Local Database:** Android Room 2.6+ with SQLite database, compile-time SQL verification, and reactive DAO Kotlin Flows.
- **Cloud Database:** MongoDB Atlas / Self-Hosted MongoDB via Mongoose 8.x ODM with strict schema schemas and compound indexing (`restaurantId`, `clientOrderUuid`, `createdAt`).

### 3.4 Communications Interfaces
- **LAN Protocol:** Local Ktor 2.3+ WebSocket server on Master Terminal, port 8080 (fallback 8443 TLS), JSON payload encoding.
- **WAN Cloud Protocol:** Encrypted WebSocket (`WSS`) connection with mutual token authentication, client heartbeat ping/pong every 15 seconds.
- **Local Peer Discovery:** mDNS (Network Service Discovery / Bonjour) service type `_dinevique-pos._tcp.local.` advertising Master IP and port.

---

## 4. System Features & Detailed Functional Requirements

### 4.1 Epic 1: Dinevique POS (Master Terminal & Orchestrator)
- **FR-POS-001 (Ktor Server & Peer Discovery):** The Master Terminal must initiate a foreground service on boot hosting a Ktor HTTP/WSS server and broadcast an mDNS service `_dinevique-pos._tcp.local.` allowing zero-configuration discovery by KDB and Handheld devices within 2 seconds.
- **FR-POS-002 (Fast Team PIN Authentication):** The system must authenticate team members via a 4-to-6 digit PIN pad within 300ms, mapping sessions to roles (*Server*, *Cashier*, *Cook*, *Manager*) and automatically locking after 60 seconds of inactivity.
- **FR-POS-003 (Floor Plan & Coursing Engine):** The system must provide visual table status (Available, Seated, Bill Printed, Dirty), seat-level order item allocation, and course state flags (`Fire` vs. `Hold`) routing only `Fire` courses to the KDB.
- **FR-POS-004 (Split Checks & Multi-Tender Settlement):** The POS must support splitting orders evenly across N parties, splitting by individual seat, transferring items between seats/checks, and accepting split tenders (Cash, Card, Gift Voucher) with penny-perfect tax rounding.
- **FR-POS-005 (Direct Hardware Peripheral Control):** The system must integrate directly with thermal and impact printers via USB/LAN and trigger cash drawer open pulses in under 100ms without cloud mediation.
- **FR-POS-006 (Offline Payment Queue & Risk Limit Engine):** The system must accept, encrypt (AES-256 GCM), and queue card authorizations during WAN outages, enforcing maximum single-transaction caps ($150 default) and cumulative offline limits ($2,500 default).

### 4.2 Epic 2: Dinevique Kitchen (Kitchen Display Board - KDB)
- **FR-KDB-001 (Real-Time Ticket Stream):** The KDB must maintain a persistent LAN WebSocket client connection to the Master Terminal, receiving and rendering incoming `TICKET_RECEIVED` payloads into high-contrast dark cards within 400ms.
- **FR-KDB-002 (Ticket State Lifecycle & Bump):** Cooks must be able to transition tickets across `NEW TICKET` (Green `#2D7D46`), `IN PROGRESS` (Amber `#D4820A`), and `BUMPED` via touch or physical bump bar, emitting `TICKET_BUMPED` back to the Master Terminal.
- **FR-KDB-003 (Station Routing, Aging Timers & Recall):** The KDB must filter tickets by preparation station (*Grill*, *Fry*, *Salad*, *Pastry*, *Expo*), render elapsed prep timers that shift visual color at 10m and 20m thresholds, and allow reopening bumped tickets within 60 minutes.
- **FR-KDB-004 (Live Item 86ing):** The kitchen lead must be able to mark items or modifier options as 86ed directly from the KDB, propagating this state to all POS, Handheld, and Online interfaces within 1 second.

### 4.3 Epic 3: Dinevique Handheld (Mobile Waitstaff POS)
- **FR-HND-001 (Ergonomic Touch Ordering):** The Handheld client must provide a single-handed, thumb-optimized ordering interface enabling complete item and modifier selection and order dispatch in 3 taps or fewer.
- **FR-HND-002 (Delegated LAN Sync Architecture):** The Handheld client must route 100% of read and write requests through the Master Terminal's local Ktor server, operating without any direct connection to the cloud or printers.
- **FR-HND-003 (Tableside Payment & Digital Receipts):** The Handheld client must support integrated NFC/EMV payment sled capture, prompt for gratuity presets (18%, 20%, 22%, Custom), and dispatch SMS/email digital receipts or trigger counter paper printing.

### 4.4 Epic 4: Dinevique Online (Consumer Web Storefront)
- **FR-ONL-001 (SSR Menu & Nested Modifier Studio):** The consumer web storefront must server-side render active menus from MongoDB, enforcing mandatory modifier selection rules, minimum/maximum constraints, and live 86ed status.
- **FR-ONL-002 (Checkout & Direct Edge Tunnel Dispatch):** The web storefront must process guest payments securely via cloud payment gateway, persist the order in MongoDB, and push the order down the WSS tunnel to the Master Terminal within 2 seconds.
- **FR-ONL-003 (Live Order Tracker):** The storefront must provide an interactive, real-time tracking interface showing current ticket progression (`Order Received` → `In the Kitchen` → `Ready for Pickup`) updated via WebSocket/SSE triggered by KDB bumps.

### 4.5 Epic 5: Dinevique Cloud (Merchant Admin & Central Gateway)
- **FR-CLD-001 (Hierarchical Menu Management):** Operators must be able to author, version, and publish multi-level menu structures (Categories, Items, Modifier Groups, Nested Modifiers, Tax Rates) and push updates down to Master Terminals via `MENU_SYNC` events.
- **FR-CLD-002 (Idempotent Cloud Ingestion Engine):** The Node.js API Gateway must ingest offline event batches from Master Terminals, using `clientEventUuid` to execute idempotent MongoDB `$set` upserts with 0 duplicate transactions.
- **FR-CLD-003 (Real-Time Analytics & Device Monitoring):** The admin portal must aggregate real-time gross/net revenue, payment breakdown, active employee shifts, and live terminal heartbeat status across all store locations.

---

## 5. Non-Functional Requirements

### 5.1 Performance & Latency SLAs
- **NFR-PERF-001:** POS UI tap-to-feedback response must complete in < 100ms.
- **NFR-PERF-002:** Handheld-to-KDB ticket transit across local Wi-Fi must complete in < 400ms (99th percentile).
- **NFR-PERF-003:** Local thermal receipt and kitchen impact printer trigger must execute in < 150ms.
- **NFR-PERF-004:** Cloud Gateway must process >= 500 concurrent replayed order events per second per cluster node.

### 5.2 Reliability & High Availability
- **NFR-REL-001:** Zero in-store orders or card payments may be lost or rejected due to WAN internet disconnection.
- **NFR-REL-002:** Outbound store-and-forward queue must automatically resume and drain in FIFO sequence within 30 seconds of WAN restoration.
- **NFR-REL-003:** Master Terminal embedded Ktor service must maintain continuous uptime via an Android sticky Foreground Service with automatic crash restart.

### 5.3 Security, Hashing & PCI-DSS Compliance
- **NFR-SEC-001:** Offline cardholder data must be encrypted immediately upon swipe/dip using AES-256 GCM with keys stored in the Android Hardware-Backed Keystore.
- **NFR-SEC-002:** Employee PINs must be salted and hashed locally using PBKDF2 or Argon2id before storage in Room SQLite.
- **NFR-SEC-003:** All external communication (Master Terminal to Cloud Gateway) must strictly enforce TLS 1.3 with pinned certificates.

### 5.4 Accessibility (WCAG 2.1 AA) & Brand Tone
- **NFR-A11Y-001:** All UI components must meet or exceed WCAG 2.1 AA contrast requirements (minimum 4.5:1 for normal text, 3.0:1 for large text/icons). Brand Amber (`#D4820A`) must never be used as text against white/ivory backgrounds; it must always be paired with Brand Ink (`#1A1A1A`) text when used as a fill.
- **NFR-A11Y-002:** All interactive buttons and touch targets must measure at least 48x48dp.
- **NFR-TONE-001:** All copy must maintain Dinevique's brand voice—calm, confident, functional, and active. Exclamation marks (`!`) and jargon are prohibited. Error messages must explicitly outline cause and recovery.

---

## 6. Verification & Validation Matrices

All functional requirements are verified through the four standard verification methods:
- **I (Inspection):** Visual examination of UI layout, brand styling, or code structure.
- **A (Analysis):** Mathematical proof, simulation, or data reconciliation check.
- **D (Demonstration):** Observation of functional operational behavior under test scenarios.
- **T (Test):** Automated unit, integration, end-to-end, or network-interruption stress test with measurable pass/fail criteria.

See [SYSTEM_TRACEABILITY_AND_TEST_CASES.md](SYSTEM_TRACEABILITY_AND_TEST_CASES.md) for individual test case mappings.
