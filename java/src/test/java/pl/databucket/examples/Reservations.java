package pl.databucket.examples;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import pl.databucket.client.Bucket;
import pl.databucket.client.DataRow;
import pl.databucket.client.Rules;

import java.util.ArrayList;
import java.util.List;

/**
 * A JUnit 5 extension that reserves rows for a test and releases them after the test - also when the test fails, so
 * no row stays reserved by a broken run.
 * <pre>
 * &#64;RegisterExtension
 * static final Reservations&lt;User&gt; USERS = new Reservations&lt;&gt;(Demo.DATABUCKET.bucket("dev-users", User.class));
 *
 * &#64;Test
 * void test() {
 *     DataRow&lt;User&gt; user = USERS.reserve(Rules.where(Rule.property("$.account.status").eq("active")));
 *     ...
 * }
 * </pre>
 * Databucket gives each free row to one caller only, so tests running in parallel never get the same row.
 */
public final class Reservations<T> implements BeforeEachCallback, AfterEachCallback {

    private final Bucket<T> bucket;

    /** The rows reserved by the test running on this thread (JUnit runs a test and its callbacks on one thread). */
    private final ThreadLocal<List<Long>> reserved = ThreadLocal.withInitial(ArrayList::new);

    public Reservations(Bucket<T> bucket) {
        this.bucket = bucket;
    }

    /** Reserves one free row matching the rules for the current test; fails the test when there is none. */
    public DataRow<T> reserve(Rules rules) {
        DataRow<T> row = bucket.reserve(rules).one()
                .orElseThrow(() -> new IllegalStateException("No free row in " + bucket.getName() + " matches " + rules));
        reserved.get().add(row.getId());
        return row;
    }

    /** Reserves several free rows at once (all or as many as there are). */
    public List<DataRow<T>> reserve(Rules rules, int limit) {
        List<DataRow<T>> rows = bucket.reserve(rules).limit(limit).fetch().getRows();
        rows.forEach(row -> reserved.get().add(row.getId()));
        return rows;
    }

    public Bucket<T> bucket() {
        return bucket;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        reserved.get().clear();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        List<Long> ids = reserved.get();
        if (!ids.isEmpty())
            bucket.release(ids.stream().mapToLong(Long::longValue).toArray());
        ids.clear();
    }
}
