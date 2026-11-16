package pl.databucket.examples;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.databucket.client.Bucket;
import pl.databucket.client.DataRow;
import pl.databucket.client.Rule;
import pl.databucket.client.Rules;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Example 6: the configuration of a test environment kept in Databucket.
 * <p>
 * The bucket "config" of the DEMO dataset holds settings per environment (DEV, INT, PRD) with defaults. Tests read the
 * settings of their environment once, before all tests. Change a URL or a feature flag in the browser and the next
 * run uses it - with the history of who changed it.
 * <p>
 * Without a class of your own the properties come as a map - handy for small or varied data.
 */
class E06_EnvironmentConfigTest {

    static final String ENVIRONMENT = Demo.setting("test.environment", "DEV");

    static final Map<String, Object> CONFIG = new TreeMap<>();

    @BeforeAll
    static void readConfiguration() {
        Bucket<Map<String, Object>> config = Demo.DATABUCKET.bucket("config");

        // the defaults first, then the values of the environment replace them
        for (String environment : new String[]{"default", ENVIRONMENT}) {
            for (DataRow<Map<String, Object>> row : config.find(Rules.where(
                    Rule.property("$.environment").eq(environment),
                    Rule.property("$.active").eq(true))).limit(100).list()) {
                Map<String, Object> setting = row.getProperties();
                CONFIG.put((String) setting.get("key"), setting.get("value"));
            }
        }
        CONFIG.forEach((key, value) -> System.out.printf("  %-22s %s%n", key, value));
    }

    @Test
    void applicationUrlOfTheEnvironment() {
        assertEquals("https://example.app." + ENVIRONMENT.toLowerCase() + ".com", CONFIG.get("APP_EXAMPLE_URL"));
        // here a real test would open the tested application at this URL
    }
}
