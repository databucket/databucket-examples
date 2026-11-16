package pl.databucket.examples;

import org.junit.jupiter.api.Test;
import pl.databucket.client.Bucket;
import pl.databucket.client.DataRow;
import pl.databucket.client.Page;
import pl.databucket.client.Rule;
import pl.databucket.client.Rules;
import pl.databucket.client.Sort;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Example 5: finding data with rules - a cheat sheet.
 * <p>
 * Finding doesn't reserve anything: use it to read data (e.g. to check the state of the test data, or for reports).
 * When a test is going to use or change the rows, reserve them (examples 1-3).
 */
class E05_FindDataTest {

    static final Bucket<User> USERS = Demo.DATABUCKET.bucket("dev-users", User.class);

    @Test
    void rules() {
        // all rules must match
        Rules activeFromGermany = Rules.where(
                Rule.property("$.account.status").eq("active"),
                Rule.property("$.address.country").eq("DE"));

        // any of them: or / and / not can be nested
        Rules notActiveOrNotVerified = Rules.where(Rule.or(
                Rule.property("$.account.status").ne("active"),
                Rule.not(Rule.property("$.account.verified").eq(true))));

        // numbers, ranges, lists, patterns, missing values
        Rules richVipsFromBigCities = Rules.where(
                Rule.property("$.account.type").eq("vip"),
                Rule.property("$.account.loyaltyPoints").between(1000, 5000),
                Rule.property("$.address.city").in("London", "Paris", "Berlin", "Warsaw", "New York"),
                Rule.property("$.contact.email").like("%@gmail.com"),
                Rule.property("$.contact.phone").isNotEmpty());

        // fields of the row: free rows only, or the rows reserved by the signed-in user
        Rules free = Rules.where(Rule.reserved().eq(false));
        Rules mine = Rules.where(Rule.owner().eq(Rule.CURRENT_USER));

        Map<String, Rules> examples = new LinkedHashMap<>();
        examples.put("active from Germany", activeFromGermany);
        examples.put("not active or not verified", notActiveOrNotVerified);
        examples.put("VIPs with 1000-5000 points from big cities", richVipsFromBigCities);
        examples.put("free", free);
        examples.put("reserved by me", mine);
        examples.forEach((name, rules) -> System.out.printf("%5d users %s%n", USERS.count(rules), name));

        assertTrue(USERS.count(activeFromGermany) > 0);
    }

    @Test
    void pagingAndSorting() {
        Rules fromPoland = Rules.where(Rule.property("$.address.country").eq("PL"));

        Page<User> page = USERS.find(fromPoland)
                .sort(Sort.desc("$.account.loyaltyPoints"))
                .limit(5)
                .page(1)
                .fetch();

        System.out.printf("%d users from Poland, %d pages; the top 5 by loyalty points:%n", page.getTotal(), page.getTotalPages());
        for (DataRow<User> user : page.getRows())
            System.out.printf("  %-25s %6d%n", user.getProperties().auth().login(), user.getProperties().account().loyaltyPoints());

        assertEquals(5, page.getRows().size());
        int first = page.getRows().get(0).getProperties().account().loyaltyPoints();
        int last = page.getRows().get(4).getProperties().account().loyaltyPoints();
        assertTrue(first >= last);
    }
}
