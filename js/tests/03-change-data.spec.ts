/**
 * Example 3: a test changes its data, so the data keeps telling the truth about the tested system.
 *
 * The test suspends a user's account (in the tested application and) in Databucket. Testers and other tests see the
 * new status, and the history of the row shows who changed it and when.
 *
 * The DEMO data is shared, so this example sets the status back after the test.
 */
import { Change, Rule, Rules } from 'databucket-client';
import { expect, test, users } from './fixtures';

const changed: number[] = [];

test.afterEach(async () => {
  if (changed.length > 0) await users.update(Change.create().set('$.account.status', 'active'), ...changed.splice(0));
});

test('suspended account is marked in Databucket', async ({ reserve }) => {
  const user = await reserve(Rules.where(Rule.property('$.account.status').eq('active')));

  // here a real test would suspend the account in the tested application, then record it:
  await users.update(Change.create().set('$.account.status', 'suspended'), user.id);
  changed.push(user.id);

  const current = await users.get(user.id);
  expect(current?.properties.account.status).toBe('suspended');

  // the history of the row (the buckets of the DEMO dataset keep it): the newest entry is our change
  const history = await users.history(user.id);
  const last = history[history.length - 1];
  expect(last.modifiedBy).toBe('demo-robot');
  console.log(`User ${user.id} changed by ${last.modifiedBy} at ${last.modifiedAt?.toISOString()}`);
});
