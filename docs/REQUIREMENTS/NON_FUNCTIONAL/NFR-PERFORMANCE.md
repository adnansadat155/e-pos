# Non-Functional Requirements: Performance & Latency SLAs

**Document ID:** `NFR-PERFORMANCE`  
**Parent:** [SRS_INDEX.md](../SRS_INDEX.md)  
**Source Ref:** [SYSTEM_PRD.md](../../SOURCES/SYSTEM_PRD.md) §8.1, [SYSTEM_SRS.md](../../SOURCES/SYSTEM_SRS.md) §5.1  
**Category:** System Performance & Responsiveness  
**Status:** DEFINED  

---

## 1. Overview & Objective
In restaurant operations, latency directly degrades service velocity, guest satisfaction, and table turnover. Dinevique enforces hard latency ceilings across UI rendering, local network transmission, peripheral I/O, and cloud batch processing.

---

## 2. Quantitative Performance Benchmarks

| Requirement ID | Operational Metric | Target Benchmark | Architectural Enforcement | Verification Method |
| :--- | :--- | :--- | :--- | :--- |
| **NFR-PERF-001** | **UI Touch-to-Feedback Latency** | `< 100ms` | Jetpack Compose reactive state bound directly to local Android Room Flows. No network round-trips on touch events. | Automated UI Macrobenchmark (`TC-POS-002`) |
| **NFR-PERF-002** | **Handheld-to-KDB Propagation** | `< 400ms` (99th pct) | Master Terminal embedded Ktor server routes LAN WebSocket frames directly without disk or WAN hops. | Distributed LAN Network Timing (`TC-KDB-001`) |
| **NFR-PERF-003** | **Hardware Print & Cash Kick** | `< 150ms` | Direct Android `UsbManager` native calls using pre-compiled ESC/POS command buffers. | USB Protocol Timing Analyzer (`TC-POS-005`) |
| **NFR-PERF-004** | **PIN Auth to Screen Render** | `< 300ms` | In-memory PIN verification against PBKDF2/Argon2 cached hash + Compose composition cache. | Instrumented Android Test (`TC-POS-002`) |
| **NFR-PERF-005** | **Cloud Ingestion Throughput** | `>= 500 events/sec` | Node.js stream ingestion with bulk MongoDB `$set` unordered batch operations. | Concurrency Load Test (`TC-CLD-002`) |
| **NFR-PERF-006** | **Web Storefront First Contentful Paint** | `< 1.2s` | Next.js Server-Side Rendering (SSR) with Edge caching and optimized Google Font subsetting. | Google Lighthouse CI Audit |

---

## 3. Degradation & Bottleneck Protections
- **Garbage Collection Optimization:** Android allocations in critical paths (ticket broadcast, printer byte generation) must use object pooling and primitive arrays to prevent GC pauses exceeding 16ms.
- **WebSocket Frame Throttling:** Non-critical telemetry is buffered and batched to prevent saturating low-bandwidth in-store Wi-Fi channels.

---
_Document: NFR-PERFORMANCE.md | Node: REQUIREMENTS/NON_FUNCTIONAL | Parent: [SRS_INDEX.md](../SRS_INDEX.md)_
