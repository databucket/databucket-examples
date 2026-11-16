package pl.databucket.examples;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import pl.databucket.client.DataRow;
import pl.databucket.client.Rule;
import pl.databucket.client.Rules;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Example 2: tests running in parallel never get the same row.
 * <p>
 * Ten tests run at the same time and each reserves "an active user from Poland". Databucket hands every free row to
 * one caller only, so each test gets its own user - no locks or test-data partitioning in the tests.
 * Parallel execution is switched on in src/test/resources/junit-platform.properties.
 */
@Execution(ExecutionMode.CONCURRENT)
class E02_ParallelTestsTest {

    @RegisterExtension
    static final Reservations<User> USERS = new Reservations<>(Demo.DATABUCKET.bucket("dev-users", User.class));

    static final Set<Long> USED = ConcurrentHashMap.newKeySet();

    @RepeatedTest(10)
    void eachTestGetsItsOwnUser() {
        DataRow<User> user = USERS.reserve(Rules.where(
                Rule.property("$.account.status").eq("active"),
                Rule.property("$.address.country").eq("PL")));

        assertTrue(USED.add(user.getId()), "user " + user.getId() + " was given to two tests");
        System.out.printf("%s got user %d%n", Thread.currentThread().getName(), user.getId());
    }

    @AfterAll
    static void tenDifferentUsers() {
        assertEquals(10, USED.size());
    }
}
