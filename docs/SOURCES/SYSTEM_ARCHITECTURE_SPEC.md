# Architectural Specification & System Design Report: Native Android Master Terminal POS Platform

---

## 1. Executive Summary
This architecture establishes a highly resilient, vertically integrated hospitality platform utilizing a Master Terminal Edge Topology. By leveraging 100% Native Android at the edge, the system achieves uncompromised hardware compatibility via direct USB/Serial drivers (`UsbManager`) and eliminates the need for an external bridging PC. A main Android tablet acts as the local orchestrator running an embedded server, ensuring the restaurant can continue placing orders, printing tickets, and processing offline payments autonomously during wide-area network (WAN) outages. A cloud-native Next.js and MongoDB backend provides centralized multi-tenant management and consumer web ordering.

---

## 2. High-Level Architectural Topology

```
+-------------------------------------------------------------------+
|                            CLOUD TIER                             |
|                                                                   |
|   +-----------------------+        +--------------------------+   |
|   | Next.js               |        | Cloud Services &         |   |
|   | Storefront &          |<------>| API Gateway              |   |
|   | Admin Portal          |  REST  | (Node.js / Express)      |   |
|   | (SSR / React)         |        +-------------+------------+   |
|   +-----------------------+                      | Mongoose GDM   |
|                                                  v                |
|                                    +--------------------------+   |
|                                    | Self-Hosted MongoDB      |   |
|                                    | (Master Menus & Orders)  |   |
|                                    +--------------------------+   |
+-------------------------------------------------------------------+
                                   ^
                                   | Encrypted WSS Tunnel /
                                   | Idempotent Sync
                                   v
+-------------------------------------------------------------------+
|                  RESTAURANT PREMISE LOCAL SUBNET                  |
|                                                                   |
|  +--------------------+               +------------------------+  |
|  | Kitchen Display    |               | Mobile Handheld POS    |  |
|  | KDB                |<------------->| (Native Android)       |  |
|  | (Native Android)   |   LAN WSS     +-----------+------------+  |
|  +---------+----------+   over Wi-Fi              |               |
|            ^                                      | LAN WSS       |
|            +------------------+                   | over Wi-Fi    |
|                               v                   v               |
|  +-------------------------------------------------------------+  |
|  | MASTER POS TERMINAL - Native Android Cashier App            |  |
|  |                                                             |  |
|  | +--------------------+  +---------------------------------+ |  |
|  | | Jetpack Compose UI |  | Embedded LAN Server             | |  |
|  | |                    |  | (Ktor WebSockets)               | |  |
|  | +--------------------+  +---------------------------------+ |  |
|  | +--------------------+  +---------------------------------+ |  |
|  | | Android Room SQLite|  | Hardware I/O Engine             | |  |
|  | | (Local Cache &     |  | (UsbManager / Native SDKs)      | |  |
|  | |  Offline Queue)    |  |                                 | |  |
|  | +--------------------+  +---------------------------------+ |  |
|  +----------------------------------+--------------------------+  |
|                                     | USB / Serial / Bluetooth    |
|                                     v                             |
|                  +-----------------------------------+            |
|                  | Hardware Peripherals              |            |
|                  | (Printers, Cash Drawers)          |            |
|                  +-----------------------------------+            |
+-------------------------------------------------------------------+
```

---

## 3. Tiered System Decomposition

### 3.1 The Edge Tier (Native Android Ecosystem)
The restaurant premise relies entirely on native Kotlin applications, allowing hardware-level control and persistent background processes.

#### A. Master POS Terminal (The Orchestrator)
This is the primary fixed tablet (e.g., the front counter register). It replaces the Node.js broker.
* **Embedded Server (Ktor):** Runs a lightweight HTTP and WebSocket server in an Android Foreground Service. It binds to a static or dynamically discovered local IP address, acting as the nerve center for all other tablets.
* **Edge Database (Android Room):** Wraps local SQLite to provide compile-time verified queries. It stores the synchronized menu tree, caches employee credentials, and acts as the First-In-First-Out (FIFO) queue for outbound cloud events and offline credit card authorizations.
* **Hardware Abstraction (UsbManager):** Uses Android's native USB Host APIs and manufacturer SDKs (e.g., Epson ePOS, StarPRNT) to communicate instantly with thermal receipt printers, impact kitchen printers, and cash drawers without network latency.
* **User Interface:** Built with Jetpack Compose for high-performance, reactive UI updates tied directly to Kotlin Flows observing the Room database.

#### B. Kitchen Display Board (KDB)
* **Client App:** A native Android application mounted on kitchen screens.
* **Network Role:** Connects to the Master Terminal's Ktor WebSocket server over the LAN.
* **Execution Flow:** Receives `TICKET_RECEIVED` events to render incoming orders. When a cook bumps a ticket, the KDB sends a `TICKET_BUMPED` event back to the Master Terminal, which relays it to the servers and the cloud.

#### C. Mobile Handhelds (Waitstaff POS)
* **Client App:** Scaled-down native Android UI optimized for 5-inch to 7-inch screens with integrated payment sleds (e.g., Sunmi V2).
* **Network Role:** Connects via Wi-Fi to the Master Terminal. It does not communicate directly with the cloud or the printers; it delegates all transactional payloads to the Master Terminal.

### 3.2 The Cloud Tier (Next.js & Node.js)
The cloud infrastructure acts as the centralized system of record and consumer gateway.
* **Storefront & Admin Web App (Next.js):** Provides a Server-Side Rendered (SSR) public website for online ordering and a secure merchant dashboard for menu building, pricing overrides, and financial analytics.
* **API Gateway & Event Hub (Node.js/Express):** Handles authentication, processes third-party delivery webhooks, and manages the persistent outbound WebSocket tunnels to the thousands of edge Master Terminals deployed globally.
* **Database (MongoDB Atlas):** A document-based NoSQL store optimized for deep, hierarchical JSON structures inherent in restaurant menus (`Items` $\rightarrow$ `Modifier Groups` $\rightarrow$ `Modifier Options`).

---

## 4. Core Operational Workflows

### 4.1 In-Store Order (Standard Operation)
1. A server inputs an order on a Mobile Handheld.
2. The Handheld sends a JSON `ORDER_CREATE` payload via local WebSocket to the Master POS Terminal.
3. The Master POS writes the order to its Android Room SQLite database.
4. The Master POS triggers the local kitchen impact printer via USB/LAN and broadcasts the ticket to the KDB WebSocket.
5. The Master POS asynchronously forwards the payload over its encrypted WAN tunnel to the Node.js Cloud Gateway.

### 4.2 Web Storefront Order (Omnichannel)
1. A consumer submits an order on the Next.js website.
2. The Cloud Gateway processes the payment and writes the canonical record to MongoDB.
3. The Gateway pushes the order payload down the persistent WebSocket tunnel directly to the Master POS Terminal.
4. The Master POS receives the order, logs it in Android Room, fires the ticket to the KDB, and chimes the local tablet to alert staff.

---

## 5. Offline Resiliency (Store-and-Forward)
The system is engineered for zero service interruption during broadband failures.
* **Failure State:** The outward WebSocket tunnel between the Master POS and the Cloud drops.
* **LAN Continuity:** The embedded Ktor server continues routing traffic between the Mobile Handhelds, KDBs, and the Room database. Kitchen tickets print normally.
* **Offline Payments:** The Master Terminal encrypts swiped/tapped card payloads and stores them in a designated Room table (`offline_payment_queue`).
* **Recovery Phase:** When the WAN connection is restored, the Master Terminal establishes a new WebSocket tunnel with the Cloud Gateway. It drains the `outbound_event_queue` chronologically.
* **Idempotent Cloud Ingestion:** The Cloud Gateway uses client-generated UUIDs (created by the Android apps) to ensure that if a payload is duplicated during unstable reconnections, MongoDB performs an update (`$set`) rather than duplicating the revenue or tickets.

---

## Component Breakdown

| Tier | Component | Technology | MVP Responsibility |
|---|---|---|---|
| **Cloud** | Storefront & Admin | Next.js | Consumer UI for online ordering, merchant UI for menu updates, reporting, and store configuration. |
| **Cloud** | API Gateway | Node.js, Express | Authenticates terminals, processes online payments, and pushes online orders down to the edge. |
| **Cloud** | Database | Self-Hosted MongoDB | System of record. Stores the canonical menu hierarchy, historical transaction logs, and user profiles. |
| **Edge** | Local Broker | Node.js, Express, SQLite | Central premise router. Accepts LAN WebSocket traffic, queues offline events, and drives local hardware (printers). |
| **Edge** | POS Terminal | React Native (Expo) | Fixed front-of-house UI on Android/iPad tablets. Reads menu from local SQLite cache, dispatches transactional events to the Broker. |
| **Edge** | Kitchen Display (KDB) | React Native (Expo) | Back-of-house UI on large tablets/displays. Subscribes to Broker's ticket stream. Cooks manually tap to transition ticket states. |
| **Edge** | Handheld POS | React Native (Expo) | Mobile waitstaff UI. Connects to Broker via Wi-Fi for tableside ordering and line busting. |

---

## MVP Data Workflows

### 1. In-Store Ordering (Standard Flow)
1. A server inputs an order on the React Native POS or mobile handheld.
2. The POS app emits a structured JSON payload (`ORDER_CREATED`) over the local WebSocket to the Local Broker.
3. The Local Broker writes the event to its SQLite database to guarantee state.
4. The Local Broker immediately broadcasts a `TICKET_RECEIVED` event to the KDB's WebSocket connection, rendering the ticket on the kitchen screen.
5. Concurrently, the Local Broker pushes the event over the outbound WAN WebSocket tunnel to the Cloud Gateway for MongoDB persistence.

### 2. Digital Web Ordering
1. A customer submits an order via the Next.js cloud storefront.
2. The Cloud Gateway verifies the payment, logs the initial state in MongoDB, and pushes the payload down the persistent WebSocket tunnel to the restaurant's Local Broker.
3. The Local Broker logs the order in SQLite, triggers an audio chime on the POS terminals, and fires the ticket to the React Native KDB.
4. As the kitchen bumps the ticket (Processing $\rightarrow$ Ready) on the KDB, it sends the state change to the Broker, which relays it back to the Cloud to update the customer's web UI.

### 3. Offline Mode & Reconnection (Store-and-Forward)
1. **WAN Fails:** The outward WebSocket tunnel drops. The LAN remains active.
2. **Local Continuity:** React Native POS and KDB devices continue sending events to the Local Broker. The Broker writes these to an `outbound_event_queue` table in SQLite. Routing and receipt printing operate normally.
3. **WAN Restored:** The Broker detects internet access and opens a new WebSocket connection to the Cloud Gateway.
4. **Queue Draining:** The Broker queries the `outbound_event_queue` and sends events chronologically in batches.
5. **Idempotency:** The Cloud Gateway uses client-generated UUIDs to ensure that if a payload is duplicated during an unstable connection, it overwrites or ignores the duplicate in MongoDB rather than double-charging the customer.