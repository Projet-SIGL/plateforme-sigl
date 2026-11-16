import { test } from '@playwright/test';

// Critical paths from the acceptance test plan (Part 4 §2, cases RT-01 to RT-04).
// Each case stays `fixme` until the screens it needs exist: a missing feature is
// never reported as a pass. Remove `fixme` when the journey is implemented, then
// fill in the steps. Test accounts are fictional and come from the seed data
// described in the plan (AP01, MA01, CP01, ECO01); credentials come from the
// environment, never from this file.

test.describe('Critical paths', () => {
  test.fixme('RT-01 register an apprentice and use the new account', async () => {
    // RH logs in; creates a valid record (school and master available);
    // re-reads it; the apprentice account can log in.
    // Expected: a single record, correct links, usable apprentice account.
  });

  test.fixme('RT-02 propose then approve an assignment', async () => {
    // CP proposes 30 %; confirms the calendar alert; MA decides.
    // Expected: precise warning; Proposed then Validated; decision traced.
  });

  test.fixme('RT-03 plan a meeting on a common slot', async () => {
    // CP searches a slot, books it, attaches the agenda, re-reads the calendars.
    // Expected: meeting persisted; participants notified; document restricted.
  });

  test.fixme('RT-04 appraisal and summary for the school', async () => {
    // Author enters and submits; HR generates the summary; MA validates it.
    // Expected: draft then submission kept; R3 applied; summary validated by MA
    // before transmission. Timing of the school read depends on decision D01.
  });
});
