/**
 * Example 4: a test creates the data it needs and removes it afterwards.
 *
 * Useful when a test needs a fresh record (e.g. a user who has never signed in) or registers a new user in the tested
 * application and wants to keep it in Databucket for other tests. The new row is inserted already reserved, so no
 * other test takes it before this one is done. Removing rows needs the ROBOT role or higher.
 */
import { randomUUID } from 'node:crypto';
import { NewData } from 'databucket-client';
import { expect, test, users } from './fixtures';
import type { User } from './user';

const created: number[] = [];

test.afterEach(async () => {
  if (created.length > 0) await users.delete(...created.splice(0));
});

test('new customer gets welcome points', async () => {
  const login = `example-${randomUUID().slice(0, 8)}`;
  const newUser: User = {
    firstName: 'Anna',
    lastName: 'Example',
    contact: { email: `${login}@example.com` },
    address: { city: 'Kraków', country: 'PL' },
    account: { type: 'standard', status: 'active', verified: false, loyaltyPoints: 0 },
    auth: { login, password: 'Secret#1234' },
  };

  // here a real test would register the user in the tested application first
  const row = await users.insert(NewData.of(newUser).reserved(true));
  created.push(row.id);

  expect(row.reserved).toBe(true);
  expect(row.properties.auth.login).toBe(login);
  console.log(`Created user ${row.id}: ${login}`);
});
