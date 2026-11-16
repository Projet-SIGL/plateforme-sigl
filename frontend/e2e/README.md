# End-to-end tests (Playwright)

Journeys RT-01 to RT-04 of the acceptance test plan (specification Part 4 §2).

```
cd frontend/e2e
npm install
npx playwright install --with-deps chromium
E2E_BASE_URL=https://<staging-url> npm test
```

Cases are `fixme` until their screens exist; they are never counted as passed.
