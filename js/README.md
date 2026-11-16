# Databucket examples - JavaScript / TypeScript

Playwright tests that take their test data from Databucket with the
[JavaScript client](https://github.com/databucket/databucket-js-client). Node.js 20.19+, TypeScript.

```shell
npm install --save-dev databucket-client
```

The tests use only the API, so no browser is needed (`npx playwright install` isn't). In your own UI tests the
fixture works the same next to `page`.

Start Databucket with the DEMO dataset (see the [main README](../README.md)), then:

```shell
npm install
npm test                                   # all examples, in parallel workers
npx playwright test tests/01-reserve-test-data.spec.ts
npm run typecheck
DATABUCKET_URL=https://databucket.example.com DATABUCKET_PROJECT="My project" npm test
```

## The files

| File | What it is |
|---|---|
| [`fixtures.ts`](tests/fixtures.ts) | The client shared by the tests of a worker (settings from environment variables) and the fixture `reserve`: reserves rows for a test and releases them after it, also when the test fails. Copy it into your project |
| [`user.ts`](tests/user.ts) | The properties of a user of the DEMO data as an interface - only what the tests use |
| [`01-reserve-test-data.spec.ts`](tests/01-reserve-test-data.spec.ts) | A test reserves the user it needs |
| [`02-parallel-tests.spec.ts`](tests/02-parallel-tests.spec.ts) | Ten parallel reservations, each gets its own user (Playwright also runs the tests in parallel workers) |
| [`03-change-data.spec.ts`](tests/03-change-data.spec.ts) | A test records a change in Databucket and checks the history |
| [`04-create-test-data.spec.ts`](tests/04-create-test-data.spec.ts) | A test creates its own record and removes it |
| [`05-find-data.spec.ts`](tests/05-find-data.spec.ts) | Rules, paging and sorting |
| [`06-environment-config.spec.ts`](tests/06-environment-config.spec.ts) | Configuration of a test environment as a map |

## In short

```ts
test('premium customer can sign in', async ({ page, reserve }) => {
  const user = await reserve(Rules.where(
    Rule.property('$.account.status').eq('active'),
    Rule.property('$.account.type').eq('premium')));

  await page.goto('/login');
  await page.getByLabel('Login').fill(user.properties.auth.login);
  // ...
});   // the user is released here
```
