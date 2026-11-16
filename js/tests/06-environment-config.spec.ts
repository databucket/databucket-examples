/**
 * Example 6: the configuration of a test environment kept in Databucket.
 *
 * The bucket "config" of the DEMO dataset holds settings per environment (DEV, INT, PRD) with defaults. The tests read
 * the settings of their environment before they start. Change a URL or a feature flag in the browser and the next run
 * uses it - with the history of who changed it.
 */
import { Rule, Rules } from 'databucket-client';
import { databucket, expect, test } from './fixtures';

const ENVIRONMENT = process.env.TEST_ENVIRONMENT || 'DEV';

interface Setting {
  key: string;
  value: string;
  environment: string;
  active: boolean;
}

const config = new Map<string, string>();

test.beforeAll(async () => {
  const bucket = databucket.bucket<Setting>('config');
  // the defaults first, then the values of the environment replace them
  for (const environment of ['default', ENVIRONMENT]) {
    const rows = await bucket.find(Rules.where(
      Rule.property('$.environment').eq(environment),
      Rule.property('$.active').eq(true))).limit(100).list();
    for (const row of rows) config.set(row.properties.key, row.properties.value);
  }
  for (const [key, value] of [...config].sort())
    console.log(`  ${key.padEnd(22)} ${value}`);
});

test('application URL of the environment', async () => {
  expect(config.get('APP_EXAMPLE_URL')).toBe(`https://example.app.${ENVIRONMENT.toLowerCase()}.com`);
  // here a real test would open the tested application at this URL
});
