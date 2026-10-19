# Authority Hierarchy & Conflict Resolution Rules — Dinevique Platform

**Parent:** [PROJECT_INDEX.md](PROJECT_INDEX.md)  
**Version:** 1.0.0  
**Last Updated:** 2026-10-19  

---

## 1. Document Authority Hierarchy

When documents disagree on requirements, data models, or interfaces, the conflict shall be resolved according to the following precedence hierarchy (Higher wins over Lower):

```
Level 1: CONTRACTS (CONTRACTS/*)
    ↓  (Inviolable API interfaces, WebSocket event frames, data schemas)
Level 2: REQUIREMENTS (REQUIREMENTS/*)
    ↓  (SRS Functional & Non-Functional Specifications, User Stories, RTM)
Level 3: ARCHITECTURE (ARCHITECTURE/*)
    ↓  (System Topology, Data Architecture, Integration Boundaries)
Level 4: MODULE SPECS (SPECS/*)
    ↓  (Component internals, Room entity definitions, UI state flows)
Level 5: IMPLEMENTATION PLANS (IMPLEMENTATION/*)
    ↓  (Roadmaps, Technical Docs, Sprints)
Level 6: CODEBASE IMPLEMENTATION
```

---

## 2. Source Document Authority

- **PRD vs. Company Profile:** In technical, architecture, or pricing specifications, [SYSTEM_PRD.md](../SOURCES/SYSTEM_PRD.md) overrides the high-level Company Profile.
- **Brand Guidelines:** In color tokens, typography, iconography, and voice tone, [SYSTEM_BRAND_AND_STYLE_GUIDELINE.md](../SOURCES/SYSTEM_BRAND_AND_STYLE_GUIDELINE.md) overrides general PRD descriptions.

---
_Document: AUTHORITY_HIERARCHY.md | Node: META | Version: 1.0.0_
