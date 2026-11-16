"""Shared by all examples: the connection to Databucket and a fixture that reserves rows for a test.

By default the examples connect to the docker compose of databucket-server with the DEMO dataset (project "Acme Shop",
user demo-robot). Change it with environment variables:

    DATABUCKET_URL=https://databucket.example.com DATABUCKET_USERNAME=tests-robot DATABUCKET_PASSWORD=... pytest

In a real project keep the password in the secrets of your CI, not in the code.
"""

import os
from collections.abc import Iterator

import pytest
from databucket import Bucket, Databucket, DataRow, Rules

from user import User


def setting(name: str, default: str) -> str:
    return os.environ.get(name) or default


# one client for the whole run: signed in once, safe to use from parallel threads
DATABUCKET = Databucket(
    setting("DATABUCKET_URL", "http://databucket.localhost"),
    username=setting("DATABUCKET_USERNAME", "demo-robot"),
    password=setting("DATABUCKET_PASSWORD", "test"),
    project=setting("DATABUCKET_PROJECT", "Acme Shop"),
)


@pytest.fixture(scope="session")
def databucket() -> Databucket:
    return DATABUCKET


@pytest.fixture(scope="session")
def users(databucket: Databucket) -> Bucket[User]:
    bucket: Bucket[User] = databucket.bucket("dev-users")
    return bucket


class Reservations:
    """Reserves rows for one test; the fixture releases them after the test."""

    def __init__(self, bucket: Bucket[User]) -> None:
        self.bucket = bucket
        self.ids: list[int] = []

    def reserve(self, rules: Rules) -> DataRow[User]:
        """One free row matching the rules; fails the test when there is none."""
        row = self.bucket.reserve(rules).one()
        if row is None:
            pytest.fail(f"No free row in {self.bucket.name} matches {rules}")
        self.ids.append(row.id)
        return row


@pytest.fixture
def reservations(users: Bucket[User]) -> Iterator[Reservations]:
    """Rows reserved by a test are released after it - also when the test fails, so no row stays reserved by a
    broken run. Databucket gives each free row to one caller only, so parallel tests never get the same row."""
    reserved = Reservations(users)
    yield reserved
    if reserved.ids:
        users.release(*reserved.ids)
