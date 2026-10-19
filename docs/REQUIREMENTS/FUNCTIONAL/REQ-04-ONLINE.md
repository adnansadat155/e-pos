# Functional Requirements: Dinevique Online (Consumer Web Storefront)

**Document ID:** `REQ-04-ONLINE`  
**Parent:** [SRS_INDEX.md](../SRS_INDEX.md)  
**Source Ref:** [SYSTEM_PRD.md](../../SOURCES/SYSTEM_PRD.md) §5.4, [SYSTEM_SRS.md](../../SOURCES/SYSTEM_SRS.md) §4.4  
**Module:** Dinevique Online (Next.js SSR Storefront)  
**Icon:** `globe` | **Accent:** Success Green (`#2D7D46`)  
**Status:** DEFINED  

---

## Table of Contents
- [1. Module Overview](#1-module-overview)
- [2. Detailed Requirements](#2-detailed-requirements)
  - [FR-ONL-001: Dynamic SSR Menu & Nested Modifier Drawer](#fr-onl-001-dynamic-ssr-menu--nested-modifier-drawer)
  - [FR-ONL-002: Direct Checkout & Edge Tunnel Order Push](#fr-onl-002-direct-checkout--edge-tunnel-order-push)
  - [FR-ONL-003: Real-Time Live Order Prep Tracker](#fr-onl-003-real-time-live-order-prep-tracker)
- [3. Cross-Reference & Traceability](#3-cross-reference--traceability)

---

## 1. Module Overview
Dinevique Online provides an optimized, consumer-facing digital ordering storefront built with Next.js (App Router, SSR, React). Connected directly to the cloud MongoDB database, it serves dynamic menus, enforces complex modifier selection rules, processes online card payments, routes orders down the WSS tunnel to the restaurant's Master Terminal, and streams real-time kitchen status back to guests.

---

## 2. Detailed Requirements

### FR-ONL-001: Dynamic SSR Menu & Nested Modifier Drawer
- **Priority:** P0 (Must Have)
- **Source:** PRD ONL-1 | SRS §4.4
- **User Story:** [US-ONL-001](../USER_STORIES.md#story-us-onl-001-dynamic-ssr-menu-rendering-with-strict-modifier-rules)
- **Description:** The storefront shall server-side render the restaurant's active menu hierarchy from MongoDB. When a dish has required modifier groups, the "Add to Order" action shall remain locked until min/max selection rules are fulfilled. Items marked 86ed in the database shall render disabled in real time.
- **Inputs:** HTTP GET request, restaurant slug, modifier selection inputs.
- **Outputs:** Fully rendered HTML payload, interactive modifier drawer state.
- **Verification Criteria:**
  - First Contentful Paint (FCP) < 1.2s on standard mobile 4G.
  - Required modifiers prevent item addition until satisfied.
- **Fulfillment:** Spec: `SPEC-ONLINE-STOREFRONT.md` | Contract: `SCHEMA-MENU-CANONICAL.md` | Test: `TC-ONL-001`

### FR-ONL-002: Direct Checkout & Edge Tunnel Order Push
- **Priority:** P0 (Must Have)
- **Source:** PRD ONL-2 | SRS §4.4
- **User Story:** [US-ONL-002](../USER_STORIES.md#story-us-onl-002-web-checkout-and-direct-wss-edge-tunnel-push)
- **Description:** Guests shall be able to complete card checkout through the web portal. Upon successful payment verification by the Node.js API Gateway, the order document shall commit to MongoDB and push immediately down the active WSS tunnel to the store's Master Terminal, automatically notifying the kitchen without manual staff tablet re-entry.
- **Inputs:** Cart payload, payment token, guest contact details.
- **Outputs:** Verified MongoDB order record, WSS downlink message to Master Terminal.
- **Verification Criteria:**
  - Order arrives on Master Terminal and chimes within 2000ms of payment authorization.
  - Zero duplicate order creation if customer double-clicks submit.
- **Fulfillment:** Spec: `SPEC-ONLINE-STOREFRONT.md` | Contract: `EVENT-ORDER-CLOUD-INGEST.md` | Test: `TC-ONL-002`

### FR-ONL-003: Real-Time Live Order Prep Tracker
- **Priority:** P0 (Must Have)
- **Source:** PRD ONL-3 | SRS §4.4
- **User Story:** [US-ONL-003](../USER_STORIES.md#story-us-onl-003-real-time-live-order-prep-tracker)
- **Description:** The confirmation page shall display a live status timeline (`Order Received` → `In the Kitchen` → `Ready for Pickup`). As line cooks update ticket states on the KDB, state transitions shall stream to the guest's browser via Server-Sent Events (SSE) or WebSocket without requiring page refreshes.
- **Inputs:** Order UUID lookup, incoming kitchen state transition event.
- **Outputs:** Dynamic SVG progress bar update, estimated completion countdown.
- **Verification Criteria:**
  - Kitchen bump on KDB updates consumer web browser status in < 1000ms.
- **Fulfillment:** Spec: `SPEC-ONLINE-STOREFRONT.md` | Contract: `EVENT-ORDER-STATUS-UPDATE.md` | Test: `TC-ONL-003`

---

## 3. Cross-Reference & Traceability

| Requirement ID | User Story | Contract / Schema | Architecture | Verification Test |
| :--- | :--- | :--- | :--- | :--- |
| `FR-ONL-001` | `US-ONL-001` | `SCHEMA-MENU-CANONICAL`| `SPEC-ONLINE-STOREFRONT.md` | `TC-ONL-001` |
| `FR-ONL-002` | `US-ONL-002` | `EVENT-ORDER-CLOUD-INGEST`| `SPEC-ONLINE-STOREFRONT.md` | `TC-ONL-002` |
| `FR-ONL-003` | `US-ONL-003` | `EVENT-ORDER-STATUS-UPDATE`| `SPEC-ONLINE-STOREFRONT.md`| `TC-ONL-003` |

---
_Document: REQ-04-ONLINE.md | Node: REQUIREMENTS/FUNCTIONAL | Parent: [SRS_INDEX.md](../SRS_INDEX.md)_
