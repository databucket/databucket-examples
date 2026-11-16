# databucket-examples

[![Examples](https://github.com/databucket/databucket-examples/actions/workflows/examples.yml/badge.svg)](https://github.com/databucket/databucket-examples/actions/workflows/examples.yml)

Small, runnable projects that show how automated tests take their test data from
[Databucket](https://github.com/databucket/databucket-server) with the client libraries. Copy what you need into your
own tests.

| Folder | Language | Client | Test framework |
|---|---|---|---|
| [`java`](java) | Java 17 | [databucket-java-client](https://github.com/databucket/databucket-java-client) (Maven Central `pl.databucket:databucket-client`) | JUnit 5 |

Python and JavaScript / TypeScript follow, with the same scenarios.

## The scenarios

| # | Example | What it shows |
|---|---|---|
| 1 | Reserve test data | A test reserves the user it needs instead of hard-coding it; the user is released after the test, also when it fails |
| 2 | Parallel tests | Ten tests run at the same time and each gets its own user |
| 3 | Change data | A test records what it changed (a suspended account); the history shows who changed it and when |
| 4 | Create test data | A test inserts a fresh record, already reserved, and removes it afterwards |
| 5 | Find data | A cheat sheet of rules, paging and sorting |
| 6 | Environment config | Settings of a test environment (URLs, feature flags) kept in Databucket |

## Run them

The examples use the DEMO dataset that comes with Databucket (project "Acme Shop", user `demo-robot`).

1. Start Databucket, e.g. the docker compose of databucket-server (needs Docker):
   ```shell
   git clone https://github.com/databucket/databucket-server.git
   cd databucket-server/docker
   docker compose up -d --build
   ```
2. Create the project with the DEMO dataset: open http://databucket.localhost, sign in as `super` / `super`,
   Management → Projects → add a project "Acme Shop" → Import dataset → DEMO. Or with Node.js 20+:
   ```shell
   node scripts/setup-demo-project.mjs
   ```
3. Run the examples of a language, e.g.:
   ```shell
   cd java
   ./mvnw test
   ```

Another Databucket, project or user: set `DATABUCKET_URL`, `DATABUCKET_PROJECT`, `DATABUCKET_USERNAME`,
`DATABUCKET_PASSWORD`. The user needs the ROBOT role or higher (example 4 removes rows). The examples change only what
they reserved or created, and set it back.

The GitHub Actions workflow does the same against the latest release of Databucket, on every change and weekly.

## More

- [Clients](https://github.com/databucket/databucket-server/wiki/Clients) and [Rules](https://github.com/databucket/databucket-server/wiki/Rules) in the wiki
- [AI agents (MCP)](https://github.com/databucket/databucket-server/wiki/AI-agents) - let an AI assistant find and reserve test data
- [databucket.pl](https://databucket.pl)
