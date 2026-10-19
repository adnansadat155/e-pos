# Contracts & Interfaces Index

**Parent:** [PROJECT_INDEX.md](../META/PROJECT_INDEX.md)  
**Node:** NODE 5 — CONTRACTS  
**Version:** 1.0.0  
**Source Baseline:** [SYSTEM_PRD.md](../SOURCES/SYSTEM_PRD.md) §3, §6  

---

## 1. Overview & Inviolability Rule

Per [AUTHORITY_HIERARCHY.md](../META/AUTHORITY_HIERARCHY.md), **CONTRACTS ARE LEVEL 1 AUTHORITY**. Code, specs, and tests must strictly conform to these documented interfaces. Any change to a contract requires an ADR in [DECISION_LOG.md](../META/DECISION_LOG.md).

```
docs/CONTRACTS/
├── CONTRACTS_INDEX.md                    # ← YOU ARE HERE
├── schemas/                              # Canonical Data Models
│   ├── SCHEMA-MENU-CANONICAL.md          # Multi-level hierarchical menu JSON
│   ├── SCHEMA-ORDER.md                   # Canonical order document & Room entity
│   ├── SCHEMA-OFFLINE-PAYMENT.md         # Encrypted offline card payload
│   └── SCHEMA-USER-SESSION.md            # Team PIN session & permissions
├── events/                               # Real-Time WebSocket Events
│   ├── EVENT-SYS-DISCOVERY.md            # mDNS payload & Ktor handshake
│   ├── EVENT-ORDER-CREATE.md             # In-store order dispatch
│   ├── EVENT-TICKET-RECEIVED.md          # KDB ticket rendering payload
│   ├── EVENT-TICKET-BUMPED.md            # KDB bump notification
│   ├── EVENT-TICKET-RECALLED.md          # KDB ticket recall event
│   ├── EVENT-ITEM-86.md                  # Item disable/86 broadcast
│   ├── EVENT-MENU-SYNC.md                # Cloud-to-Edge menu broadcast
│   ├── EVENT-ORDER-CLOUD-INGEST.md       # Online order push to edge
│   └── EVENT-ORDER-STATUS-UPDATE.md      # Live tracker customer stream
├── api/                                  # HTTP REST Contracts
│   ├── API-ORDERS-INGEST.md              # Offline queue bulk ingestion
│   └── API-ANALYTICS-SUMMARY.md          # Real-time dashboard analytics
└── internal/                             # Native & Peripheral Contracts
    ├── CONTRACT-HARDWARE-IO.md           # ESC/POS byte formats & drawer kick
    └── CONTRACT-LAN-WEBSOCKET.md         # Local Wi-Fi framing & heartbeats
```

---
_Contracts Index | Node: CONTRACTS | Parent: [PROJECT_INDEX.md](../META/PROJECT_INDEX.md) | Version: 1.0.0_
