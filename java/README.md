# Databucket examples - Java

JUnit 5 tests that take their test data from Databucket with the
[Java client](https://github.com/databucket/databucket-java-client). Java 17+, Maven (the wrapper `./mvnw` is included).

```xml
<dependency>
  <groupId>pl.databucket</groupId>
  <artifactId>databucket-client</artifactId>
  <version>2.4.0</version>
</dependency>
```

Start Databucket with the DEMO dataset (see the [main README](../README.md)), then:

```shell
./mvnw test                                  # all examples
./mvnw test -Dtest=E01_ReserveTestDataTest   # one of them
./mvnw test -Ddatabucket.url=https://databucket.example.com -Ddatabucket.project="My project"
```

## The files

| File | What it is |
|---|---|
| [`Demo`](src/test/java/pl/databucket/examples/Demo.java) | The client shared by all tests: signed in once, safe for parallel tests; settings from system properties or environment variables |
| [`User`](src/test/java/pl/databucket/examples/User.java) | The properties of a user of the DEMO data as Java records - only what the tests use |
| [`Reservations`](src/test/java/pl/databucket/examples/Reservations.java) | A JUnit 5 extension: reserves rows for a test and releases them after it, also when the test fails. Copy it into your project |
| [`E01_ReserveTestDataTest`](src/test/java/pl/databucket/examples/E01_ReserveTestDataTest.java) | A test reserves the user it needs |
| [`E02_ParallelTestsTest`](src/test/java/pl/databucket/examples/E02_ParallelTestsTest.java) | Ten parallel tests, each with its own user ([`junit-platform.properties`](src/test/resources/junit-platform.properties) switches parallel runs on) |
| [`E03_ChangeDataTest`](src/test/java/pl/databucket/examples/E03_ChangeDataTest.java) | A test records a change in Databucket and checks the history |
| [`E04_CreateTestDataTest`](src/test/java/pl/databucket/examples/E04_CreateTestDataTest.java) | A test creates its own record and removes it |
| [`E05_FindDataTest`](src/test/java/pl/databucket/examples/E05_FindDataTest.java) | Rules, paging and sorting |
| [`E06_EnvironmentConfigTest`](src/test/java/pl/databucket/examples/E06_EnvironmentConfigTest.java) | Configuration of a test environment as a map |

## In short

```java
@RegisterExtension
static final Reservations<User> USERS = new Reservations<>(Demo.DATABUCKET.bucket("dev-users", User.class));

@Test
void premiumCustomerCanSignIn() {
    DataRow<User> user = USERS.reserve(Rules.where(
            Rule.property("$.account.status").eq("active"),
            Rule.property("$.account.type").eq("premium")));

    // sign in to the tested application with user.getProperties().auth() ...
}   // the user is released here
```

The tests are in `src/test/java`, as in a project that keeps its tests next to the tested code; in a separate test
project they work the same.
