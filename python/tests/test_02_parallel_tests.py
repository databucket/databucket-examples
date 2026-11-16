"""Example 2: tests running in parallel never get the same row.

Ten workers reserve "an active user from Poland" at the same time. Databucket hands every free row to one caller only,
so each gets its own user - no locks or test-data partitioning in the tests.

Here the workers are threads, so the example can check that the users differ. With pytest-xdist
(``pip install pytest-xdist``, ``pytest -n 10``) whole tests run in parallel processes and the fixture
``reservations`` works the same way.
"""

from concurrent.futures import ThreadPoolExecutor
import threading

from databucket import Bucket, Rule, Rules

from user import User

ACTIVE_FROM_POLAND = Rules.where(
    Rule.property("$.account.status").eq("active"),
    Rule.property("$.address.country").eq("PL"),
)


def test_each_worker_gets_its_own_user(users: Bucket[User]) -> None:
    def worker(_: int) -> int:
        user = users.reserve(ACTIVE_FROM_POLAND).one()
        assert user is not None, "No free active user from Poland"
        print(f"{threading.current_thread().name} got user {user.id}")
        return user.id

    with ThreadPoolExecutor(max_workers=10) as pool:
        ids = list(pool.map(worker, range(10)))

    try:
        assert len(set(ids)) == 10, f"a user was given twice: {sorted(ids)}"
    finally:
        users.release(*ids)
