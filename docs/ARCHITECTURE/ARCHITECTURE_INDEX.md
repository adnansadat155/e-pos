# System Architecture Index

**Parent:** [PROJECT_INDEX.md](../META/PROJECT_INDEX.md)  
**Node:** NODE 4 — ARCHITECTURE  
**Version:** 1.0.0  
**Source Baseline:** [SYSTEM_ARCHITECTURE_SPEC.md](../SOURCES/SYSTEM_ARCHITECTURE_SPEC.md), [SYSTEM_PRD.md](../SOURCES/SYSTEM_PRD.md) §3  

---

## 1. Overview & Architectural Map

Dinevique employs a hybrid **Native Android Master Terminal Edge Topology** connected to a **Next.js, Node.js, and MongoDB Cloud Tier**. The edge eliminates external Windows server boxes by running an embedded Ktor HTTP/WebSocket engine inside an Android Foreground Service, writing directly to local Room SQLite.

```
docs/ARCHITECTURE/
├── ARCHITECTURE_INDEX.md                 # ← YOU ARE HERE
├── SYSTEM_ARCHITECTURE.md                # System Topology, Edge/Cloud Split, Protocols
├── DATA_ARCHITECTURE.md                  # Room SQLite & MongoDB Data Flow & Schemas
├── INTEGRATION_MAP.md                    # Hardware I/O, Payment Gateways & External APIs
├── SECURITY_ARCHITECTURE.md              # Keystore Encryption, PBKDF2 Auth & Threat Model
└── modules/                              # Per-module architectural specifications
    ├── ARCH-POS-MASTER.md
    ├── ARCH-KITCHEN-KDB.md
    ├── ARCH-HANDHELD-MOBILE.md
    ├── ARCH-ONLINE-STOREFRONT.md
    └── ARCH-CLOUD-GATEWAY.md
```

---

## 2. Core Architecture Documents

| Document | Purpose | Key Focus |
| :--- | :--- | :--- |
| [SYSTEM_ARCHITECTURE.md](SYSTEM_ARCHITECTURE.md) | Master Topology | Master Terminal Android orchestrator + Cloud Gateway |
| [DATA_ARCHITECTURE.md](DATA_ARCHITECTURE.md) | Data Flows & Stores | Room SQLite offline queues + MongoDB hierarchical menus |
| [INTEGRATION_MAP.md](INTEGRATION_MAP.md) | External Integration | UsbManager, ESC/POS, Sunmi V2, Stripe/Payment processor |
| [SECURITY_ARCHITECTURE.md](SECURITY_ARCHITECTURE.md) | Security & Cryptography | AES-256 GCM offline queue, hardware keystore, TLS 1.3 |

---

_Architecture Index | Node: ARCHITECTURE | Parent: [PROJECT_INDEX.md](../META/PROJECT_INDEX.md) | Version: 1.0.0_
