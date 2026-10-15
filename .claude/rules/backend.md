---
paths:
  - "services/**"
---

# Spring Boot microservice rules

- A service **never** reads another service's database: it goes through that
  service's REST API or an event.
- Internal organization (layered or hexagonal) follows the specification document
  (Part 2 §3); do not mix both styles.
- Every endpoint is documented with OpenAPI and listed in the REST API table of the
  specification (Part 2 §4). A new endpoint or event must be added there.
- State transitions (Project, Task, Assignment, Appraisal) follow the state diagrams
  in `docs/uml/`; any transition not defined there is rejected.
- Access rights are checked in the service according to the access rights matrix,
  even if the gateway has already validated the token.
- Unit tests with JUnit 5 and Mockito; integration tests with Testcontainers.
- Configuration and secrets through environment variables, never hard-coded.
