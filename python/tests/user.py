"""The properties of a row in the buckets dev-users, int-users and prd-users of the DEMO dataset.

Only the parts the examples use, as TypedDicts: the properties are plain dicts, the types only help your editor.
"""

from typing import TypedDict


class Contact(TypedDict, total=False):
    email: str
    phone: str


class Address(TypedDict, total=False):
    city: str
    country: str


class Account(TypedDict, total=False):
    type: str  # standard / premium / vip
    status: str  # active / suspended / locked / closed
    verified: bool
    loyaltyPoints: int


class Auth(TypedDict, total=False):
    login: str
    password: str


class User(TypedDict, total=False):
    firstName: str
    lastName: str
    contact: Contact
    address: Address
    account: Account
    auth: Auth
