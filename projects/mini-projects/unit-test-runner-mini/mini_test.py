class TestFailure(Exception):
    pass


def assert_equal(actual, expected, message=""):
    if actual != expected:
        raise TestFailure(f"{message} attendu={expected!r} obtenu={actual!r}")


class MiniTestRunner:
    def __init__(self):
        self.tests = []

    def test(self, func):
        self.tests.append(func)
        return func

    def run(self):
        passed = failed = 0
        for test in self.tests:
            try:
                test()
                print(f"PASS {test.__name__}")
                passed += 1
            except TestFailure as e:
                print(f"FAIL {test.__name__}: {e}")
                failed += 1
            except Exception as e:
                print(f"ERROR {test.__name__}: {e}")
                failed += 1
        print(f"\n{passed} reussis, {failed} echoues sur {len(self.tests)}")
        return failed == 0


runner = MiniTestRunner()


@runner.test
def test_addition():
    assert_equal(2 + 2, 4, "addition simple")


@runner.test
def test_addition_qui_echoue_expres():
    assert_equal(2 + 2, 5, "addition volontairement fausse pour demo")


if __name__ == "__main__":
    runner.run()
