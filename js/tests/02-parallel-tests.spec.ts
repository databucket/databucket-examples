/**
 * Example 2: tests running in parallel never get the same row.
 *
 * Ten reservations of "an active user from Poland" are sent at the same time. Databucket hands every free row to one
 * caller only, so each gets its own user - no locks or test-data partitioning in the tests.
 *
 * Here the ten callers are promises of one test, so the example can check that the users differ. Playwright runs
 * whole tests in parallel workers (`fullyParallel` in playwright.config.ts) and the fixture `reserve` works the
 * same way there.
 */
import { Rule, Rules } from 'databucket-client';
import { expect, test, users } from './fixtures';

const activeFromPoland = Rules.where(
  Rule.property('$.account.status').eq('active'),
  Rule.property('$.address.country').eq('PL'));

test('each caller gets its own user', async () => {
  const reserved = await Promise.all(Array.from({ length: 10 }, () => users.reserve(activeFromPoland).one()));
  const ids = reserved.map((user) => {
    if (!user) throw new Error('No free active user from Poland');
    return user.id;
  });
  try {
    console.log(`Reserved users: ${ids.join(', ')}`);
    expect(new Set(ids).size).toBe(10);
  } finally {
    await users.release(...ids);
  }
});
