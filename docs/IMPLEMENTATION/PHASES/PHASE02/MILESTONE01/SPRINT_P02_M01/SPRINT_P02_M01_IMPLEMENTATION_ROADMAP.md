# Sprint Implementation Roadmap: Phase 02 / Milestone 01

**Sprint ID:** `SPRINT_P02_M01`  
**Phase:** Phase 02 — In-Store Operations  
**Milestone:** Milestone 01 — POS Core UI & Handheld Flow  
**Target Duration:** Sprint 1  
**Status:** 🟡 **IN EXECUTION**  
**Traceability:** PRD §5.1, §5.3 | SRS §4.1, §4.3 | REQ-01-POS (`FR-POS-003`), REQ-03-HANDHELD (`FR-HND-001`, `FR-HND-002`) | User Stories: `US-POS-003`, `US-HND-001`  

---

## 1. Objectives & Deliverables

This sprint delivers the core front-of-house UI and interaction layer for Dinevique:
1. **Interactive Floor Plan (`FloorPlanScreen.kt`):** Visual table grid supporting occupancy tracking, live elapsed seated duration timers, guest party sizes, and server assignment chips.
2. **High-Density POS Order Workspace (`OrderWorkspaceScreen.kt`):** Two-pane layout for 10-15" touch displays. Left pane hosts the live active check with item list, seat assignments, course hold tags, and subtotal/tax calculations. Right pane presents category tabs and responsive menu item grids.
3. **Ergonomic Modifier Modal (`ModifierBottomSheet.kt`):** Bottom sheet modal for selecting item variations, enforcing minimum/maximum group requirements, calculating price deltas in cents, and appending customized items to the check.
4. **Sunmi V2 Handheld 3-Tap Order Flow (`HandheldOrderScreen.kt`):** Thumb-optimized portrait UI enabling waitstaff to select an item, confirm mandatory modifiers, and dispatch to the kitchen in 3 taps or fewer.
5. **Reactive ViewModels & LAN Integration:** `FloorPlanViewModel`, `OrderWorkspaceViewModel`, and `HandheldOrderViewModel` wired to Room SQLite and the embedded Ktor LAN WebSocket server.
6. **Automated Verification:** Comprehensive test suite validating state transitions, modifier constraints, calculation accuracy, and touch responsiveness.

---

## 2. Task Breakdown

| Task ID | Task Name | Component | Scope / Details | Success Criteria |
| :--- | :--- | :--- | :--- | :--- |
| **T01** | Table Floor Plan UI | `apps/android/app-pos` | Compose table grid, 4 occupancy states, live duration timer | Renders tables with color-coded states & timers |
| **T02** | POS Order Workspace | `apps/android/app-pos` | Split-screen workspace, category chips, item cards grid | Dual-pane layout with reactive check summary |
| **T03** | Item Modifier Modal | `apps/android/app-pos` | Bottom sheet with min/max selection enforcement & price deltas | Prevents submission until required groups are satisfied |
| **T04** | Handheld 3-Tap Flow | `apps/android/app-handheld`| Thumb-friendly 5-7" UI, category carousel, 3-tap order CTA | Complete order dispatched in <= 3 taps |
| **T05** | ViewModels & Edge State | `app-pos` & `app-handheld` | State management, Room SQLite binding, Ktor event emission | Reactive state updates & event dispatch |
| **T06** | Test Suite & Validation | `test/pos-handheld.test.mjs`| Table transitions, modifier math, 3-tap verification | 100% test pass rate & SLA compliance |

---

## 3. Strict Design & Brand Standards

- **Color Palette:**
  - Background: Brand Onyx (`#0D0D0D`)
  - Surfaces/Cards: Brand Slate (`#1E2A38`)
  - Primary CTA Fill: Brand Amber (`#D4820A`)
  - Primary CTA Text: Brand Ink (`#1A1A1A`) strictly (WCAG AA 4.5:1 compliant)
  - Text Primary: Brand White (`#FFFFFF`)
  - Text Muted: Silver/Light Gray (`#A0AEC0`)
- **Typography:** Inter font family with strict tabular numerals for all monetary amounts (`$XX.XX`).
- **Touch Targets:** Minimum 48x48dp touch targets on POS, 56x56dp for primary action CTAs on Handheld.
- **Copy:** Zero exclamation marks across all UI labels and toasts.

---
_Sprint Roadmap | Parent: [MILESTONE01_INDEX.md](../MILESTONE01_INDEX.md) | Status: APPROVED_
