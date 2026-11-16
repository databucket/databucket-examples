"""Example 3: a test changes its data, so the data keeps telling the truth about the tested system.

The test suspends a user's account (in the tested application and) in Databucket. Testers and other tests see the
new status, and the history of the row shows who changed it and when.

The DEMO data is shared, so this example sets the status back after the test.
"""

from collections.abc import Iterator

import pytest
from databucket import Bucket, Change, Rule, Rules

from conftest import Reservations
from user import User


@pytest.fixture
def changed(users: Bucket[User]) -> Iterator[list[int]]:
    """The ids of the users the test suspended; their status is set back afterwards."""
    ids: list[int] = []
    yield ids
    if ids:
        users.update(Change().set("$.account.status", "active"), *ids)


def test_suspended_account_is_marked_in_databucket(
    reservations: Reservations, users: Bucket[User], changed: list[int]
) -> None:
    user = reservations.reserve(Rules.where(Rule.property("$.account.status").eq("active")))

    # here a real test would suspend the account in the tested application, then record it:
    users.update(Change().set("$.account.status", "suspended"), user.id)
    changed.append(user.id)

    current = users.get(user.id)
    assert current is not None
    assert current.properties["account"]["status"] == "suspended"

    # the history of the row (the buckets of the DEMO dataset keep it): the newest entry is our change
    last = users.history(user.id)[-1]
    assert last.modified_by == "demo-robot"
    print(f"User {user.id} changed by {last.modified_by} at {last.modified_at}")
