# Non-Functional Requirements: Reliability & High Availability

**Document ID:** `NFR-RELIABILITY`  
**Parent:** [SRS_INDEX.md](../SRS_INDEX.md)  
**Source Ref:** [SYSTEM_PRD.md](../../SOURCES/SYSTEM_PRD.md) §4.3, §8.1; [SYSTEM_SRS.md](../../SOURCES/SYSTEM_SRS.md) §5.2  
**Category:** High Availability & Fault Tolerance  
**Status:** DEFINED  

---

## 1. Overview & Resiliency Mandate
Dinevique guarantees that restaurant operations never halt when wide-area internet connectivity fails. The platform treats internet connectivity as an asynchronous transport layer rather than a synchronous operational dependency.

---

## 2. Resiliency Requirements

| Requirement ID | Operational Area | Benchmark / Constraint | Architectural Enforcement |
| :--- | :--- | :--- | :--- |
| **NFR-REL-001** | **WAN Outage Zero Dropped Orders** | `0 lost orders` | In-store ordering, kitchen printing, and KDB coordination continue uninterrupted via local Ktor over LAN Wi-Fi. |
| **NFR-REL-002** | **Store-and-Forward FIFO Queue** | `100% order capture` | All mutations commit locally to Room SQLite `outbound_event_queue` during WAN loss. |
| **NFR-REL-003** | **Automatic Reconnection & Drain** | `< 30s` post restoration | Master Terminal monitors WAN tunnel health; on reconnect, it drains the queue chronologically with exponential backoff. |
| **NFR-REL-004** | **Master Foreground Persistence** | `99.99% edge uptime` | Ktor runs inside an Android Foreground Service with `START_STICKY`, wake-locks, and unkillable system priority. |
| **NFR-REL-005** | **Idempotent Ingestion Guarantee** | `0 duplicates` | Cloud Gateway enforces client UUID keys with MongoDB `$set` upserts, preventing duplicates during unstable reconnects. |

---

## 3. Disaster Recovery & Edge Failure Scenarios

### 3.1 Scenario: Master Tablet Hardware Failure
- If the Master Terminal suffers fatal hardware damage, an auxiliary Android terminal (e.g., secondary counter POS or KDB) can be promoted to Master by importing the local Room SQLite snapshot or downloading the latest cloud snapshot, restoring in-store operations in < 5 minutes.

### 3.2 Scenario: Wi-Fi Router Crash
- Handheld terminals and KDB buffer state transitions in local memory and attempt automatic reconnection every 2000ms until the Wi-Fi AP completes rebooting.

---
_Document: NFR-RELIABILITY.md | Node: REQUIREMENTS/NON_FUNCTIONAL | Parent: [SRS_INDEX.md](../SRS_INDEX.md)_
