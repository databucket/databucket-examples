"""Example 5: finding data with rules - a cheat sheet.

Finding doesn't reserve anything: use it to read data (e.g. to check the state of the test data, or for reports).
When a test is going to use or change the rows, reserve them (examples 1-3).
"""

from databucket import Bucket, Rule, Rules, Sort

from user import User


def test_rules(users: Bucket[User]) -> None:
    examples = {
        # all rules must match
        "active from Germany": Rules.where(
            Rule.property("$.account.status").eq("active"),
            Rule.property("$.address.country").eq("DE"),
        ),
        # any of them: | (or), & (and) and ~ (not) can be nested - or Rule.or_(), Rule.and_(), Rule.not_()
        "not active or not verified": Rules.where(
            Rule.property("$.account.status").ne("active") | ~Rule.property("$.account.verified").eq(True)
        ),
        # numbers, ranges, lists, patterns, missing values
        "VIPs with 1000-5000 points from big cities": Rules.where(
            Rule.property("$.account.type").eq("vip"),
            Rule.property("$.account.loyaltyPoints").between(1000, 5000),
            Rule.property("$.address.city").in_("London", "Paris", "Berlin", "Warsaw", "New York"),
            Rule.property("$.contact.email").like("%@gmail.com"),
            Rule.property("$.contact.phone").is_not_empty(),
        ),
        # fields of the row: free rows only, or the rows reserved by the signed-in user
        "free": Rules.where(Rule.reserved().eq(False)),
        "reserved by me": Rules.where(Rule.owner().eq(Rule.CURRENT_USER)),
    }
    for name, rules in examples.items():
        print(f"{users.count(rules):5} users {name}")

    assert users.count(examples["active from Germany"]) > 0


def test_paging_and_sorting(users: Bucket[User]) -> None:
    page = (
        users.find(Rules.where(Rule.property("$.address.country").eq("PL")))
        .sort(Sort.desc("$.account.loyaltyPoints"))
        .limit(5)
        .page(1)
        .fetch()
    )

    print(f"{page.total} users from Poland, {page.total_pages} pages; the top 5 by loyalty points:")
    for user in page.rows:
        print(f"  {user.properties['auth']['login']:25} {user.properties['account']['loyaltyPoints']:6}")

    points = [user.properties["account"]["loyaltyPoints"] for user in page.rows]
    assert len(points) == 5
    assert points == sorted(points, reverse=True)
