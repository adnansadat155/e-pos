# Software Requirements Specification (SRS) Index

**Parent:** [PROJECT_INDEX.md](../META/PROJECT_INDEX.md)  
**Node:** NODE 3 — REQUIREMENTS  
**Version:** 1.0.0  
**Last Updated:** 2026-10-19  
**Source Baseline:** [SYSTEM_SRS.md](../SOURCES/SYSTEM_SRS.md), [SYSTEM_PRD.md](../SOURCES/SYSTEM_PRD.md)  

---

## 1. Overview & Navigation

The Requirements node contains the canonical decomposition of what the Dinevique platform must achieve. Every requirement is uniquely identified, categorized, prioritized, and linked forward into architecture, contracts, specifications, and test cases.

```
docs/REQUIREMENTS/
├── SRS_INDEX.md                                # ← YOU ARE HERE
├── USER_STORIES.md                             # Canonical User Story Catalog
├── REQUIREMENTS_TRACEABILITY_MATRIX.md         # Full Bidirectional Traceability
├── FUNCTIONAL/                                 # Functional Requirements by Module
│   ├── REQ-01-POS.md                           # Dinevique POS (Master Terminal)
│   ├── REQ-02-KDB.md                           # Dinevique Kitchen (KDB)
│   ├── REQ-03-HANDHELD.md                      # Dinevique Handheld (Mobile POS)
│   ├── REQ-04-ONLINE.md                        # Dinevique Online (Storefront)
│   └── REQ-05-CLOUD.md                         # Dinevique Cloud (Admin & Gateway)
└── NON_FUNCTIONAL/                             # Quality Attributes & Constraints
    ├── NFR-PERFORMANCE.md                      # Latency SLAs & Throughput
    ├── NFR-RELIABILITY.md                      # Offline Resiliency & Failover
    ├── NFR-SECURITY.md                         # Encryption, PINs, & PCI-DSS
    └── NFR-ACCESSIBILITY-BRAND.md              # WCAG AA, Typography & Voice
```

---

## 2. Functional Requirements Inventory

| Module / Epic | Requirement File | Requirements Defined | Priority Breakdown | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Dinevique POS** | [REQ-01-POS.md](FUNCTIONAL/REQ-01-POS.md) | `FR-POS-001` – `FR-POS-006` | P0: 6, P1: 0 | DEFINED |
| **Dinevique Kitchen** | [REQ-02-KDB.md](FUNCTIONAL/REQ-02-KDB.md) | `FR-KDB-001` – `FR-KDB-004` | P0: 2, P1: 2 | DEFINED |
| **Dinevique Handheld** | [REQ-03-HANDHELD.md](FUNCTIONAL/REQ-03-HANDHELD.md) | `FR-HND-001` – `FR-HND-003` | P0: 2, P1: 1 | DEFINED |
| **Dinevique Online** | [REQ-04-ONLINE.md](FUNCTIONAL/REQ-04-ONLINE.md) | `FR-ONL-001` – `FR-ONL-003` | P0: 3, P1: 0 | DEFINED |
| **Dinevique Cloud** | [REQ-05-CLOUD.md](FUNCTIONAL/REQ-05-CLOUD.md) | `FR-CLD-001` – `FR-CLD-003` | P0: 3, P1: 0 | DEFINED |

---

## 3. Non-Functional Requirements Inventory

| Quality Category | Specification File | Key Metrics & Guardrails | Status |
| :--- | :--- | :--- | :--- |
| **Performance** | [NFR-PERFORMANCE.md](NON_FUNCTIONAL/NFR-PERFORMANCE.md) | UI < 100ms, LAN WebSocket < 400ms, Print < 150ms | DEFINED |
| **Reliability** | [NFR-RELIABILITY.md](NON_FUNCTIONAL/NFR-RELIABILITY.md) | 0 dropped orders in WAN outage, FIFO drain < 30s | DEFINED |
| **Security** | [NFR-SECURITY.md](NON_FUNCTIONAL/NFR-SECURITY.md) | AES-256 GCM offline queue, PBKDF2/Argon2 PINs, TLS 1.3 | DEFINED |
| **A11y & Brand** | [NFR-ACCESSIBILITY-BRAND.md](NON_FUNCTIONAL/NFR-ACCESSIBILITY-BRAND.md) | WCAG 2.1 AA (>= 4.5:1), 48dp targets, zero exclamation marks | DEFINED |

---

## 4. User Stories & Traceability Pointers

- **User Story Catalog:** [USER_STORIES.md](USER_STORIES.md) (All 19 stories with Gherkin acceptance criteria)
- **Requirements Traceability Matrix (RTM):** [REQUIREMENTS_TRACEABILITY_MATRIX.md](REQUIREMENTS_TRACEABILITY_MATRIX.md) (PRD ↔ SRS ↔ Stories ↔ Architecture ↔ Contracts ↔ Tests ↔ Sprints)

---

_SRS Index | Node: REQUIREMENTS | Parent: [PROJECT_INDEX.md](../META/PROJECT_INDEX.md) | Version: 1.0.0_
