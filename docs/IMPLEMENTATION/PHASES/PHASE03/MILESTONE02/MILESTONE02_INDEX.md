# Phase 03: Milestone 02 — Live Order Tracker & Analytics

**Parent:** [PHASE03_INDEX.md](../PHASE03_INDEX.md)  
**Milestone:** Phase 03 — Milestone 02  
**Status:** 🟢 **COMPLETED (100% AUDIT PASS)**  
**Lead:** DDADS Autonomous Delivery Agent  

---

## 1. Milestone Goal

Deliver real-time omnichannel transparency and operational intelligence by building:
1. **Customer Live Order Status Tracker (`/track/[orderUuid]`):** Low-latency Server-Sent Events (SSE) stream delivering real-time status progression (`ORDER_PLACED` $\rightarrow$ `IN_PREPARATION` $\rightarrow$ `READY_FOR_PICKUP` $\rightarrow$ `COMPLETED`), pickup token visualization, and order receipt breakdown.
2. **Restaurant Management Analytics Dashboard (`/admin/analytics`):** Real-time aggregation of gross sales, net revenue, table turn rates, average check size, category distribution, and hourly volume heatmaps.
3. **Master Terminal Heartbeat & Edge Health Monitor (`/admin/terminals`):** Live telemetry of connected in-store edge master terminals, LAN sync queue depth, Ktor LAN ping latency, and peripheral hardware health (receipt printer, kitchen impact, cash drawer).
4. **Cloud Gateway Real-time Streaming API:** SSE connection handling (`GET /api/v1/orders/:orderUuid/stream`), status broadcast mechanism (`POST /api/v1/orders/:orderUuid/status`), terminal heartbeat ingest, and sales analytics endpoints.

---

## 2. Sprint Index

| Sprint ID | Focus | Status | Audit Result | Directory |
| :--- | :--- | :--- | :--- | :--- |
| **`SPRINT_P03_M02`** | Live Tracker, Cloud Analytics, Edge Health & SSE Stream | 🟢 **COMPLETED** | **100% PASS** | [SPRINT_P03_M02/](SPRINT_P03_M02/SPRINT_P03_M02_IMPLEMENTATION_ROADMAP.md) |

---

## 3. Architecture & Edge Topology Integration

```mermaid
graph TD
    subgraph Customer Tier
        WebClient["Customer Browser (/track/[orderUuid])"]
    end

    subgraph Store Manager Tier
        AnalyticsUI["Admin Analytics (/admin/analytics)"]
        TerminalsUI["Edge Terminal Monitor (/admin/terminals)"]
    end

    subgraph Cloud Gateway Tier
        SSEHub["SSE Event Stream Hub (/orders/:uuid/stream)"]
        AnalyticsEngine["Analytics Aggregation Controller"]
        HeartbeatHub["Terminal Heartbeat Registry"]
    end

    subgraph In-Store Local LAN Tier
        MasterTerminal["Android Master Terminal (Ktor :8080)"]
        KDB["Kitchen Display (KDB) Bump Bar"]
    end

    KDB -->|LAN Bump Action| MasterTerminal
    MasterTerminal -->|WSS Edge Tunnel / Ingest| SSEHub
    MasterTerminal -->|Heartbeat Ping (30s)| HeartbeatHub
    SSEHub -->|Push Event (< 10ms)| WebClient
    HeartbeatHub -->|Telemetry State| TerminalsUI
    AnalyticsEngine -->|Aggregated Metrics| AnalyticsUI
```

---
_Phase 03 / Milestone 02 Index | Parent: [PHASE03_INDEX.md](../PHASE03_INDEX.md) | Status: COMPLETED_
