# Non-Functional Requirements: Security & Compliance

**Document ID:** `NFR-SECURITY`  
**Parent:** [SRS_INDEX.md](../SRS_INDEX.md)  
**Source Ref:** [SYSTEM_PRD.md](../../SOURCES/SYSTEM_PRD.md) §8.2, [SYSTEM_SRS.md](../../SOURCES/SYSTEM_SRS.md) §5.3  
**Category:** Security, Cryptography & Regulatory Compliance  
**Status:** DEFINED  

---

## 1. Overview & PCI-DSS Scope
Dinevique handles sensitive cardholder data, team authentication credentials, and commercial sales records. All security implementations must adhere to strict PCI-DSS v4.0 standards, zero-trust edge topology principles, and modern cryptographic best practices.

---

## 2. Security Requirements

| Requirement ID | Domain | Specification & Standard | Implementation Detail |
| :--- | :--- | :--- | :--- |
| **NFR-SEC-001** | **Offline Card Data Encryption** | AES-256 GCM (Authenticated Encryption) | Encrypted immediately upon swipe/dip. Encryption keys reside in the hardware-backed Android KeyStore / TEE. |
| **NFR-SEC-002** | **Zero Plaintext Storage** | PCI-DSS Requirement 3.4 | PAN, CVV, and track data are never logged or stored in plaintext in memory or Room SQLite tables. |
| **NFR-SEC-003** | **Employee PIN Security** | PBKDF2 with HMAC-SHA256 (>= 100k iterations) or Argon2id | PINs are salted per user and hashed. PIN validation is strictly local on the Master Terminal. |
| **NFR-SEC-004** | **Transport Layer Security** | TLS 1.3 Mandatory | Edge-to-cloud WSS communication requires TLS 1.3 with pinned certificates and token rotation. |
| **NFR-SEC-005** | **LAN Device Authentication** | Pre-Shared Secret / Mutual Auth | In-store WebSockets require an encrypted handshake token exchanging restaurant UUID and terminal MAC hash. |
| **NFR-SEC-006** | **Role-Based Access Control (RBAC)**| Least Privilege Principle | Discrete permissions: *Server* (orders/checks), *Cashier* (tenders), *Cook* (tickets), *Manager* (voids/86/offline). |

---

## 3. Regulatory Certification Gateways
Per PRD §8.2, offline card authorization and EMV/NFC hardware sleds (e.g., Sunmi V2) must pass formal PCI-DSS point-to-point encryption (P2PE) audit and payment processor terminal certification prior to commercial deployment.

---
_Document: NFR-SECURITY.md | Node: REQUIREMENTS/NON_FUNCTIONAL | Parent: [SRS_INDEX.md](../SRS_INDEX.md)_
