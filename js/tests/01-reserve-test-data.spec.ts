/**
 * Example 1: a test takes the user it needs from Databucket instead of hard-coding it.
 *
 * The user is reserved for the test, so no other test (or tester) gets it at the same time, and released after the
 * test by the fixture `reserve` (fixtures.ts) - also when the test fails.
 */
import { Rule, Rules } from 'databucket-client';
import { expect, test } from './fixtures';

test('premium customer can sign in', async ({ reserve }) => {
  const user = await reserve(Rules.where(
    Rule.property('$.account.status').eq('active'),
    Rule.property('$.account.type').eq('premium'),
    Rule.property('$.address.country').in('GB', 'DE')));

  const { login, password } = user.properties.auth;
  // here a real test would sign in to the tested application, e.g. with Playwright:
  //   await page.getByLabel('Login').fill(login); await page.getByLabel('Password').fill(password); ...

  expect(user.properties.account.type).toBe('premium');
  expect(user.reserved).toBe(true);
  expect(user.owner).toBe('demo-robot');
  expect(password).toBeTruthy();
  console.log(`Test user ${user.id}: ${login} (${user.properties.address.country})`);
});

test('locked account can not sign in', async ({ reserve }) => {
  const user = await reserve(Rules.where(Rule.property('$.account.status').eq('locked')));

  // here a real test would check that the application refuses to sign in with these credentials
  expect(user.properties.account.status).toBe('locked');
});
