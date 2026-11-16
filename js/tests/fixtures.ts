/**
 * Shared by all examples: the connection to Databucket and a fixture that reserves rows for a test.
 *
 * By default the examples connect to the docker compose of databucket-server with the DEMO dataset (project
 * "Acme Shop", user demo-robot). Change it with environment variables:
 *
 *   DATABUCKET_URL=https://databucket.example.com DATABUCKET_USERNAME=tests-robot DATABUCKET_PASSWORD=... npm test
 *
 * In a real project keep the password in the secrets of your CI, not in the code.
 */
import { test as base } from '@playwright/test';
import { Databucket, type Bucket, type DataRow, type Rules } from 'databucket-client';
import type { User } from './user';

// one client per worker: signed in once, shared by the tests of the worker
export const databucket = new Databucket({
  url: process.env.DATABUCKET_URL || 'http://databucket.localhost',
  username: process.env.DATABUCKET_USERNAME || 'demo-robot',
  password: process.env.DATABUCKET_PASSWORD || 'test',
  project: process.env.DATABUCKET_PROJECT || 'Acme Shop',
});

export const users: Bucket<User> = databucket.bucket<User>('dev-users');

/** Reserves one free row matching the rules for the current test; fails the test when there is none. */
export type Reserve = (rules: Rules) => Promise<DataRow<User>>;

export const test = base.extend<{ reserve: Reserve }>({
  /**
   * Rows reserved by a test are released after it - also when the test fails, so no row stays reserved by a broken
   * run. Databucket gives each free row to one caller only, so parallel tests never get the same row.
   */
  reserve: async ({}, use) => {
    const ids: number[] = [];
    try {
      await use(async (rules) => {
        const row = await users.reserve(rules).one();
        if (!row) throw new Error(`No free row in ${users.name} matches ${JSON.stringify(rules)}`);
        ids.push(row.id);
        return row;
      });
    } finally {
      if (ids.length > 0) await users.release(...ids);
    }
  },
});

export { expect } from '@playwright/test';
