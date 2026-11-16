"""Example 4: a test creates the data it needs and removes it afterwards.

Useful when a test needs a fresh record (e.g. a user who has never signed in) or registers a new user in the tested
application and wants to keep it in Databucket for other tests. The new row is inserted already reserved, so no other
test takes it before this one is done. Removing rows needs the ROBOT role or higher.
"""

from collections.abc import Iterator
import uuid

import pytest
from databucket import Bucket, NewData

from user import User


@pytest.fixture
def created(users: Bucket[User]) -> Iterator[list[int]]:
    """The ids of the rows the test created; they are removed afterwards."""
    ids: list[int] = []
    yield ids
    if ids:
        users.delete(*ids)


def test_new_customer_gets_welcome_points(users: Bucket[User], created: list[int]) -> None:
    login = f"example-{uuid.uuid4().hex[:8]}"
    new_user: User = {
        "firstName": "Anna",
        "lastName": "Example",
        "contact": {"email": f"{login}@example.com"},
        "address": {"city": "Kraków", "country": "PL"},
        "account": {"type": "standard", "status": "active", "verified": False, "loyaltyPoints": 0},
        "auth": {"login": login, "password": "Secret#1234"},
    }

    # here a real test would register the user in the tested application first
    row = users.insert(NewData(new_user).reserved(True))
    created.append(row.id)

    assert row.reserved
    assert row.properties["auth"]["login"] == login
    print(f"Created user {row.id}: {login}")
