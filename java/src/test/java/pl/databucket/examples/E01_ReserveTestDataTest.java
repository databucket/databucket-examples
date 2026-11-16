package pl.databucket.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import pl.databucket.client.DataRow;
import pl.databucket.client.Rule;
import pl.databucket.client.Rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Example 1: a test takes the user it needs from Databucket instead of hard-coding it.
 * <p>
 * The user is reserved for the test, so no other test (or tester) gets it at the same time, and released after the
 * test by {@link Reservations} - also when the test fails.
 */
class E01_ReserveTestDataTest {

    @RegisterExtension
    static final Reservations<User> USERS = new Reservations<>(Demo.DATABUCKET.bucket("dev-users", User.class));

    @Test
    void premiumCustomerCanSignIn() {
        DataRow<User> user = USERS.reserve(Rules.where(
                Rule.property("$.account.status").eq("active"),
                Rule.property("$.account.type").eq("premium"),
                Rule.property("$.address.country").in("GB", "DE")));

        User.Auth auth = user.getProperties().auth();
        // here a real test would sign in to the tested application with auth.login() and auth.password()

        assertEquals("premium", user.getProperties().account().type());
        assertTrue(user.isReserved());
        assertEquals("demo-robot", user.getOwner());
        System.out.printf("Test user %d: %s (%s)%n", user.getId(), auth.login(), user.getProperties().address().country());
    }

    @Test
    void lockedAccountCanNotSignIn() {
        DataRow<User> user = USERS.reserve(Rules.where(Rule.property("$.account.status").eq("locked")));

        // here a real test would check that the application refuses to sign in with these credentials
        assertEquals("locked", user.getProperties().account().status());
    }
}
