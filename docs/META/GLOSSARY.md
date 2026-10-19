# Glossary — Dinevique Platform

**Parent:** [PROJECT_INDEX.md](PROJECT_INDEX.md)
**Purpose:** Canonical terminology. Every domain term used across the KB is defined here exactly once.

---

| Term | Definition | Source |
|---|---|---|
| **Master Terminal** | The primary Dinevique POS tablet (fixed, front-of-house). Runs the embedded Ktor server, Android Room SQLite, and hardware I/O engine. Acts as the local orchestrator for all in-premises devices. | PRD §3.2 |
| **KDB** | Kitchen Display Board — the Dinevique Kitchen module. A native Android display mounted in the kitchen for real-time ticket management. | PRD §3.2 |
| **Ktor** | Kotlin-native embedded HTTP and WebSocket server framework running inside an Android Foreground Service on the Master Terminal. | PRD §3.2 |
| **Android Room** | Kotlin ORM over SQLite, used as the edge database on the Master Terminal. Provides compile-time verified queries and Kotlin Flow observability. | PRD §3.2 |
| **WAN** | Wide Area Network — the internet connection between the restaurant premise and cloud services. The platform continues essential operations during WAN outages. | PRD §4.3 |
| **LAN** | Local Area Network — the in-premises Wi-Fi / wired network over which the Master Terminal communicates with KDB and Handheld devices via LAN WebSockets. | PRD §3.1 |
| **Store-and-Forward** | The mechanism by which the Master Terminal queues outbound cloud events (`outbound_event_queue`) during WAN outages and drains them upon reconnection. | PRD §4.3 |
| **Idempotent Sync** | Cloud ingestion strategy using client-generated UUIDs; duplicated payloads are upserted (`$set`) not inserted, preventing double orders or double charges. | PRD §4.3, §5.5 |
| **outbound_event_queue** | Android Room SQLite table storing FIFO ordered cloud-bound events during WAN outage. | PRD §6.2 |
| **offline_payment_queue** | Android Room SQLite table storing encrypted card authorization payloads captured during WAN outage. | PRD §6.2 |
| **clientEventUuid** | Client-generated UUID attached to every event payload at the edge; used by the cloud gateway to deduplicate replayed events. | PRD §4.3 |
| **TICKET_RECEIVED** | WebSocket event emitted by the Master Terminal to the KDB when a new order ticket is ready for kitchen preparation. | PRD §3.2, §4.1 |
| **TICKET_BUMPED** | WebSocket event emitted by the KDB back to the Master Terminal when a cook marks a ticket as complete (bumped). | PRD §3.2, §4.2 |
| **ORDER_CREATE** | WebSocket payload emitted by a Handheld or POS terminal to the Master Terminal to create a new order. | PRD §4.1 |
| **MENU_SYNC** | WebSocket event pushed by the Cloud Gateway down the WSS tunnel to the Master Terminal to update the local menu cache. | PRD §5.5 |
| **WSS Tunnel** | Encrypted WebSocket Secure (WSS) connection maintained between the Master Terminal and the Node.js Cloud Gateway for real-time bidirectional cloud sync. | PRD §3.1 |
| **mDNS** | Multicast DNS — used for peer discovery on the local subnet so Handhelds and KDBs can find the Master Terminal's Ktor server without manual IP entry. | PRD §5.1 |
| **Brand Amber** | Primary interactive accent color `#D4820A`. Used for CTAs, active ticket frames, and active icon states. WCAG FAIL on light backgrounds for small text. | Brand Guidelines §4 |
| **Brand Onyx** | Primary dark background `#0D0D0D`. Used for KDB canvas, dark hero sections. | Brand Guidelines §4 |
| **Brand Slate** | Secondary dark surface `#1E2A38`. Used for ticket card frames, sidebars. | Brand Guidelines §4 |
| **Brand Ivory** | Light background `#F7F5F1`. Used for Cloud admin and Online storefront content areas. | Brand Guidelines §4 |
| **P0** | MVP Must-Have priority — required for v1.0 pilot deployment. | PRD §5 |
| **P1** | Commercial v1.0 Target — required for full-scale production deployment. | PRD §5 |
| **UsbManager** | Android native USB Host API used to communicate directly with thermal printers, impact printers, and cash drawers. | PRD §3.2 |
| **Jetpack Compose** | Android modern declarative UI toolkit used for the POS and KDB interfaces. | PRD §3.2 |
| **Kotlin Flows** | Reactive stream library in Kotlin used to observe Android Room database changes and update Compose UI reactively. | PRD §3.2 |

---

_Glossary | Node: META | Parent: [PROJECT_INDEX.md](PROJECT_INDEX.md) | Updated: 2026-10-19_
