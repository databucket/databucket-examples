package pl.databucket.examples;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pl.databucket.client.Change;
import pl.databucket.client.DataRow;
import pl.databucket.client.HistoryEntry;
import pl.databucket.client.Rule;
import pl.databucket.client.Rules;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Example 3: a test changes its data, so the data keeps telling the truth about the tested system.
 * <p>
 * The test suspends a user's account (in the tested application and) in Databucket. Testers and other tests see the
 * new status, and the history of the row shows who changed it and when.
 * <p>
 * The DEMO data is shared, so this example sets the status back after the test.
 */
class E03_ChangeDataTest {

    @RegisterExtension
    static final Reservations<User> USERS = new Reservations<>(Demo.DATABUCKET.bucket("dev-users", User.class));

    private Long changedUserId;

    @Test
    void suspendedAccountIsMarkedInDatabucket() {
        DataRow<User> user = USERS.reserve(Rules.where(Rule.property("$.account.status").eq("active")));

        // here a real test would suspend the account in the tested application, then record it:
        USERS.bucket().update(Change.create().set("$.account.status", "suspended"), user.getId());
        changedUserId = user.getId();

        DataRow<User> changed = USERS.bucket().get(user.getId()).orElseThrow();
        assertEquals("suspended", changed.getProperties().account().status());

        // the history of the row (the buckets of the DEMO dataset keep it): the newest entry is our change
        List<HistoryEntry<User>> history = USERS.bucket().history(user.getId());
        HistoryEntry<User> last = history.get(history.size() - 1);
        assertEquals("demo-robot", last.getModifiedBy());
        System.out.printf("User %d changed by %s at %s%n", user.getId(), last.getModifiedBy(), last.getModifiedAt());
    }

    @AfterEach
    void restoreTheDemoData() {
        if (changedUserId != null)
            USERS.bucket().update(Change.create().set("$.account.status", "active"), changedUserId);
    }
}
