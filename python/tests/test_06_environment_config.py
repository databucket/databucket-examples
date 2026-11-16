"""Example 6: the configuration of a test environment kept in Databucket.

The bucket "config" of the DEMO dataset holds settings per environment (DEV, INT, PRD) with defaults. The tests read
the settings of their environment once per run. Change a URL or a feature flag in the browser and the next run uses
it - with the history of who changed it.
"""

import os
from typing import Any

import pytest
from databucket import Databucket, Rule, Rules

ENVIRONMENT = os.environ.get("TEST_ENVIRONMENT") or "DEV"


@pytest.fixture(scope="session")
def config(databucket: Databucket) -> dict[str, Any]:
    bucket = databucket.bucket("config")
    settings: dict[str, Any] = {}
    # the defaults first, then the values of the environment replace them
    for environment in ("default", ENVIRONMENT):
        rows = bucket.find(
            Rules.where(Rule.property("$.environment").eq(environment), Rule.property("$.active").eq(True))
        ).limit(100).list()
        for row in rows:
            settings[row.properties["key"]] = row.properties["value"]
    for key in sorted(settings):
        print(f"  {key:22} {settings[key]}")
    return settings


def test_application_url_of_the_environment(config: dict[str, Any]) -> None:
    assert config["APP_EXAMPLE_URL"] == f"https://example.app.{ENVIRONMENT.lower()}.com"
    # here a real test would open the tested application at this URL
