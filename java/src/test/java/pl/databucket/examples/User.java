package pl.databucket.examples;

/**
 * The properties of a row in the buckets dev-users, int-users and prd-users of the DEMO dataset - only the parts the
 * examples use. The client ignores the other properties, so a test describes just what it needs.
 */
public record User(String firstName, String lastName, Contact contact, Address address, Account account, Auth auth) {

    public record Contact(String email, String phone) {
    }

    public record Address(String city, String country) {
    }

    /** type: standard / premium / vip, status: active / suspended / locked / closed */
    public record Account(String type, String status, Boolean verified, Integer loyaltyPoints) {
    }

    public record Auth(String login, String password) {
    }
}
