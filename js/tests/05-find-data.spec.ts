/**
 * Example 5: finding data with rules - a cheat sheet.
 *
 * Finding doesn't reserve anything: use it to read data (e.g. to check the state of the test data, or for reports).
 * When a test is going to use or change the rows, reserve them (examples 1-3).
 */
import { Rule, Rules, Sort } from 'databucket-client';
import { expect, test, users } from './fixtures';

test('rules', async () => {
  const examples: Record<string, Rules> = {
    // all rules must match
    'active from Germany': Rules.where(
      Rule.property('$.account.status').eq('active'),
      Rule.property('$.address.country').eq('DE')),

    // any of them: or / and / not can be nested
    'not active or not verified': Rules.where(Rule.or(
      Rule.property('$.account.status').ne('active'),
      Rule.not(Rule.property('$.account.verified').eq(true)))),

    // numbers, ranges, lists, patterns, missing values
    'VIPs with 1000-5000 points from big cities': Rules.where(
      Rule.property('$.account.type').eq('vip'),
      Rule.property('$.account.loyaltyPoints').between(1000, 5000),
      Rule.property('$.address.city').in('London', 'Paris', 'Berlin', 'Warsaw', 'New York'),
      Rule.property('$.contact.email').like('%@gmail.com'),
      Rule.property('$.contact.phone').isNotEmpty()),

    // fields of the row: free rows only, or the rows reserved by the signed-in user
    'free': Rules.where(Rule.reserved().eq(false)),
    'reserved by me': Rules.where(Rule.owner().eq(Rule.CURRENT_USER)),
  };

  for (const [name, rules] of Object.entries(examples))
    console.log(`${String(await users.count(rules)).padStart(5)} users ${name}`);

  expect(await users.count(examples['active from Germany'])).toBeGreaterThan(0);
});

test('paging and sorting', async () => {
  const page = await users.find(Rules.where(Rule.property('$.address.country').eq('PL')))
    .sort(Sort.desc('$.account.loyaltyPoints'))
    .limit(5)
    .page(1)
    .fetch();

  console.log(`${page.total} users from Poland, ${page.totalPages} pages; the top 5 by loyalty points:`);
  for (const user of page.rows)
    console.log(`  ${user.properties.auth.login.padEnd(25)} ${String(user.properties.account.loyaltyPoints).padStart(6)}`);

  const points = page.rows.map((user) => user.properties.account.loyaltyPoints);
  expect(points).toHaveLength(5);
  expect(points).toEqual([...points].sort((a, b) => b - a));
});
