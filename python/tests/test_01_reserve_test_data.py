"""Example 1: a test takes the user it needs from Databucket instead of hard-coding it.

The user is reserved for the test, so no other test (or tester) gets it at the same time, and released after the test
by the fixture ``reservations`` (conftest.py) - also when the test fails.
"""

from databucket import Rule, Rules

from conftest import Reservations


def test_premium_customer_can_sign_in(reservations: Reservations) -> None:
    user = reservations.reserve(
        Rules.where(
            Rule.property("$.account.status").eq("active"),
            Rule.property("$.account.type").eq("premium"),
            Rule.property("$.address.country").in_("GB", "DE"),
        )
    )

    auth = user.properties["auth"]
    # here a real test would sign in to the tested application with auth["login"] and auth["password"]

    assert user.properties["account"]["type"] == "premium"
    assert user.reserved
    assert user.owner == "demo-robot"
    print(f"Test user {user.id}: {auth['login']} ({user.properties['address']['country']})")


def test_locked_account_can_not_sign_in(reservations: Reservations) -> None:
    user = reservations.reserve(Rules.where(Rule.property("$.account.status").eq("locked")))

    # here a real test would check that the application refuses to sign in with these credentials
    assert user.properties["account"]["status"] == "locked"
