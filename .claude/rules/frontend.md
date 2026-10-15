---
paths:
  - "frontend/**"
---

# React client rules

- Strict TypeScript: no `any` without a comment explaining why.
- API calls only through TanStack Query; never call a service directly:
  everything goes through the gateway.
- Routes and allowed roles follow the navigation tree of the specification
  document (Part 1 §6) and the access rights matrix (Part 2 §6).
- Folder organization follows the specification document (Part 2 §3).
- Component tests with Vitest and React Testing Library.
- No token or secret in client code.
