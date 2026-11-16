# Databucket examples - Python

pytest tests that take their test data from Databucket with the
[Python client](https://github.com/databucket/databucket-python-client). Python 3.10+.

```shell
pip install databucket-client
```

Start Databucket with the DEMO dataset (see the [main README](../README.md)), then:

```shell
python -m venv .venv
.venv/Scripts/activate              # Windows; Linux / macOS: source .venv/bin/activate
pip install -r requirements.txt
pytest                              # all examples
pytest tests/test_01_reserve_test_data.py
DATABUCKET_URL=https://databucket.example.com DATABUCKET_PROJECT="My project" pytest
```

## The files

| File | What it is |
|---|---|
| [`conftest.py`](tests/conftest.py) | The client shared by all tests (signed in once, safe for threads; settings from environment variables) and the fixture `reservations`: reserves rows for a test and releases them after it, also when the test fails. Copy it into your project |
| [`user.py`](tests/user.py) | The properties of a user of the DEMO data as `TypedDict`s - only what the tests use |
| [`test_01_reserve_test_data.py`](tests/test_01_reserve_test_data.py) | A test reserves the user it needs |
| [`test_02_parallel_tests.py`](tests/test_02_parallel_tests.py) | Ten parallel workers, each with its own user (with `pytest-xdist` whole tests run in parallel the same way) |
| [`test_03_change_data.py`](tests/test_03_change_data.py) | A test records a change in Databucket and checks the history |
| [`test_04_create_test_data.py`](tests/test_04_create_test_data.py) | A test creates its own record and removes it |
| [`test_05_find_data.py`](tests/test_05_find_data.py) | Rules, paging and sorting |
| [`test_06_environment_config.py`](tests/test_06_environment_config.py) | Configuration of a test environment as a dict |

## In short

```python
def test_premium_customer_can_sign_in(reservations):
    user = reservations.reserve(Rules.where(
        Rule.property("$.account.status").eq("active"),
        Rule.property("$.account.type").eq("premium"),
    ))

    # sign in to the tested application with user.properties["auth"] ...
    # the user is released after the test
```
