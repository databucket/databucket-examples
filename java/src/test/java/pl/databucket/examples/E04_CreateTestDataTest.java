package pl.databucket.examples;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pl.databucket.client.Bucket;
import pl.databucket.client.DataRow;
import pl.databucket.client.NewData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Example 4: a test creates the data it needs and removes it afterwards.
 * <p>
 * Useful when a test needs a fresh record (e.g. a user who has never signed in) or registers a new user in the tested
 * application and wants to keep it in Databucket for other tests. The new row is inserted already reserved, so no
 * other test takes it before this one is done. Removing rows needs the ROBOT role or higher.
 */
class E04_CreateTestDataTest {

    static final Bucket<User> USERS = Demo.DATABUCKET.bucket("dev-users", User.class);

    private final List<Long> created = new ArrayList<>();

    @Test
    void newCustomerGetsWelcomePoints() {
        String login = "example-" + UUID.randomUUID().toString().substring(0, 8);
        User newUser = new User("Anna", "Example",
                new User.Contact(login + "@example.com", null),
                new User.Address("Kraków", "PL"),
                new User.Account("standard", "active", false, 0),
                new User.Auth(login, "Secret#1234"));

        // here a real test would register the user in the tested application first
        DataRow<User> row = USERS.insert(NewData.of(newUser).reserved(true));
        created.add(row.getId());

        assertTrue(row.isReserved());
        assertEquals(login, row.getProperties().auth().login());
        System.out.printf("Created user %d: %s%n", row.getId(), login);
    }

    @AfterEach
    void removeCreatedData() {
        if (!created.isEmpty())
            USERS.delete(created.stream().mapToLong(Long::longValue).toArray());
    }
}
