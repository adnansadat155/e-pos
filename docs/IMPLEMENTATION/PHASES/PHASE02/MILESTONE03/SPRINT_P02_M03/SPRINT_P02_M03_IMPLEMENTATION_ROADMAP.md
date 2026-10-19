# Sprint Implementation Roadmap: Phase 02 / Milestone 03

**Sprint ID:** `SPRINT_P02_M03`  
**Phase:** Phase 02 — In-Store Operations  
**Milestone:** Milestone 03 — Direct Hardware I/O — Printers & Cash Drawer  
**Target Duration:** Sprint 3  
**Status:** 🟡 **IN EXECUTION**  
**Traceability:** PRD §5.1, §5.5, §6.3 | SRS §4.1, §4.5 | REQ-01-POS (`FR-POS-005`), NFR-PERFORMANCE (`NFR-PERF-003`) | User Stories: `US-POS-005`  

---

## 1. Objectives & Deliverables

This sprint delivers direct hardware peripheral control for the Master Terminal:
1. **ESC/POS Thermal Receipt Builder (`EscPosBuilder.kt`):** Generates binary ESC/POS bytecode for standard 80mm thermal printers with brand header, tabular items, modifier deltas, tax/tip breakdown, tender details, and paper cutting.
2. **Kitchen Impact Dot Matrix Builder (`KitchenImpactBuilder.kt`):** Formats 9-pin kitchen impact tickets with double-width headers and dynamic two-color ribbon switching (`ESC r 1` for red alerts/modifications and `ESC r 0` for black standard text).
3. **Cash Drawer Kick Controller (`CashDrawerController.kt`):** Solenoid pulse generation (`ESC p m t1 t2`) over printer RJ12 peripheral ports and direct USB relays, achieving $< 100\text{ms}$ drawer kick SLA upon cash settlement.
4. **Hardware Peripheral Manager & Paper-Out Routing (`HardwareManager.kt`):** Manages Android `UsbManager` device connections, monitors printer status flags, and transparently reroutes print jobs to backup printers upon paper jams or empty roll faults.
5. **Verification Test Suite (`hardware-io.test.mjs`):** Automated test suite verifying binary byte sequences, two-color ribbon commands, drawer kick timing SLA, and paper-out failover routing.

---

## 2. Task Breakdown

| Task ID | Task Name | Component | Scope / Details | Success Criteria |
| :--- | :--- | :--- | :--- | :--- |
| **T01** | ESC/POS Receipt Engine | `apps/android/app-pos` | 80mm 48-col format, brand layout, tender details, cut command | Byte-accurate ESC/POS output matching receipt schema |
| **T02** | Kitchen Impact Engine | `apps/android/app-pos` | Two-color ribbon switching, double-size text, allergy red flags | `ESC r 1` emitted for modifiers/allergies |
| **T03** | Cash Drawer Controller | `apps/android/app-pos` | Solenoid pulse generator (`0x1B 0x70`), manual No-Sale override | Drawer kicks in < 100ms on cash tender |
| **T04** | Peripheral Manager | `apps/android/app-pos` | UsbManager bindings, health monitoring, failover queue | Auto-reroutes to backup printer on paper-out |
| **T05** | Test Suite & Validation | `test/hardware-io.test.mjs` | ESC/POS bytecode verification, failover & kick latency tests | 100% test pass rate & SLA compliance |

---

## 3. Strict Hardware & SLA Standards

- **Drawer Kick Latency:** The physical solenoid kick command MUST be emitted within $< 100\text{ms}$ of cash tender confirmation.
- **Receipt Print Latency:** Receipt stream transmission initiates within $< 150\text{ms}$ of order settlement.
- **Paper Width Compatibility:** Supports standard 80mm (48 columns) and 58mm (32 columns) thermal rolls.
- **Resilience:** Unreachable or out-of-paper printers must never crash the POS or block transaction settlement; jobs must spool to the failover queue.

---
_Sprint Roadmap | Parent: [MILESTONE03_INDEX.md](../MILESTONE03_INDEX.md) | Status: APPROVED_
