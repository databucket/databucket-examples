package pl.databucket.examples;

import pl.databucket.client.Databucket;

/**
 * The connection to Databucket shared by all examples: one client, signed in once, safe to use from parallel tests.
 * <p>
 * By default it connects to the docker compose of databucket-server with the DEMO dataset (project "Acme Shop",
 * user demo-robot). Change it with system properties or environment variables:
 * <pre>
 * ./mvnw test -Ddatabucket.url=https://databucket.example.com -Ddatabucket.username=tests-robot
 * DATABUCKET_URL=... DATABUCKET_USERNAME=... DATABUCKET_PASSWORD=... DATABUCKET_PROJECT=... ./mvnw test
 * </pre>
 * In a real project keep the password in the secrets of your CI, not in the code.
 */
public final class Demo {

    public static final Databucket DATABUCKET = Databucket.builder(setting("databucket.url", "http://databucket.localhost"))
            .password(setting("databucket.username", "demo-robot"), setting("databucket.password", "test"))
            .project(setting("databucket.project", "Acme Shop"))
            .build();

    private Demo() {
    }

    /** A system property (databucket.url), else an environment variable (DATABUCKET_URL), else the default. */
    static String setting(String property, String defaultValue) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank())
            value = System.getenv(property.toUpperCase().replace('.', '_'));
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
