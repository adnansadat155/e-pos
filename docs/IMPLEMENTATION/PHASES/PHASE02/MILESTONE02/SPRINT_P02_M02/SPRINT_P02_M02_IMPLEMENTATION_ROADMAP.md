# Sprint Implementation Roadmap: Phase 02 / Milestone 02

**Sprint ID:** `SPRINT_P02_M02`  
**Phase:** Phase 02 — In-Store Operations  
**Milestone:** Milestone 02 — Split Checks & Pass 86ing  
**Target Duration:** Sprint 2  
**Status:** 🟡 **IN EXECUTION**  
**Traceability:** PRD §5.1, §5.2, §6.2 | SRS §4.1, §4.2 | REQ-01-POS (`FR-POS-003`, `FR-POS-004`), REQ-02-KDB (`FR-KDB-001`, `FR-KDB-002`) | User Stories: `US-POS-004`, `US-KDB-001`, `US-KDB-002`  

---

## 1. Objectives & Deliverables

This sprint delivers complex in-store dining logic:
1. **Split Check Engine & UI (`SplitCheckEngine.kt`, `SplitCheckModal.kt`):**
   - Split check by seat numbers with item dragging/reassignment.
   - Split check evenly across $N$ parties ($2 \le N \le 20$) with mathematical penny-perfect rounding (guaranteeing exact 0-cent discrepancy between original check and split checks sum).
   - Multi-tender payment recording per check (`CASH`, `CARD`, `OFFLINE_CARD`, `GIFT_CARD`).
2. **Course Hold & Fire Engine (`CoursingEngine.kt`):**
   - Dynamic course tags (`APPETIZER`, `ENTREE`, `DESSERT`, `BEVERAGE`) and hold states (`HOLD` vs `FIRE`).
   - Firing courses dispatches `COURSE_FIRED` LAN event.
   - KDB ticket view updates to render newly fired items in real time while held items remain hidden.
3. **KDB Bump Micro-Interaction & Station Filtering (`KitchenTicketScreen.kt`):**
   - 4-stage kitchen ticket workflow: `NEW_TICKET` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `READY` $\rightarrow$ `BUMPED`.
   - Station-based view filters: `ALL`, `GRILL`, `FRY`, `SALAD`, `BAR`, `EXPO`.
   - One-tap bump interaction with sound/visual feedback and LAN event broadcast.
4. **Pass 86ing Availability Engine (`Pass86Manager.kt`):**
   - Fast toggle for out-of-stock items ("86'd").
   - Instant LAN WebSocket broadcast (`MENU_ITEM_86`) across all POS, Handheld, and KDB terminals within < 200ms.
   - POS and Handheld instantly disable ordering of 86'd items with visual indicator (strikethrough / badge).
5. **Verification & Stress Test Suite (`split-86ing.test.mjs`):**
   - Mathematical proof and test suite covering $N$-way splits ($N=2..15$) across varied cent totals proving 0-cent discrepancy.
   - Seat split item assignment and tax redistribution validation.
   - Course hold/fire ticket isolation testing.
   - Pass 86ing broadcast and order inhibition test.

---

## 2. Task Breakdown

| Task ID | Task Name | Component | Scope / Details | Success Criteria |
| :--- | :--- | :--- | :--- | :--- |
| **T01** | Split Check Engine | `apps/android/app-pos` | Seat splits, N-way splits, penny-perfect rounding | 0-cent discrepancy guaranteed across all splits |
| **T02** | Coursing Engine | `apps/android/app-pos` | Course hold/fire state machine, LAN event dispatch | Held items isolated until explicitly fired |
| **T03** | KDB Bump & Stations | `apps/android/app-kitchen` | 4-stage lifecycle, station filters (`GRILL`, `BAR`, etc.) | Ticket transitions in < 100ms with broadcast |
| **T04** | Pass 86ing Engine | `app-pos` & `app-kitchen` | 86ing toggle, LAN broadcast (`MENU_ITEM_86`) | Propagates in < 200ms & disables item selection |
| **T05** | Test Suite & Validation | `test/split-86ing.test.mjs` | Mathematical proofs, coursing & 86ing simulation | 100% test pass rate & SLA compliance |

---

## 3. Strict Brand & Performance Standards

- **Rounding Integrity:** The penny-perfect distribution algorithm MUST satisfy:
  $$\sum_{i=1}^N \text{SplitTotal}_i = \text{OriginalTotal}$$
  with zero cents rounding discrepancy.
- **KDB Touch Target:** Minimum 64x64dp touch target for Kitchen Bump button.
- **LAN Propagation SLA:** 86ing broadcast and Course Fire events propagate across edge peers in $< 200\text{ms}$.
- **Brand Tokens:** Maintain Brand Onyx (`#0D0D0D`), Brand Slate (`#1E2A38`), Brand Amber (`#D4820A`), and Brand Ink (`#1A1A1A`) strictly.

---
_Sprint Roadmap | Parent: [MILESTONE02_INDEX.md](../MILESTONE02_INDEX.md) | Status: APPROVED_
