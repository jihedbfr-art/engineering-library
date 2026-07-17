class SagaStep:
    def __init__(self, name, action, compensation):
        self.name, self.action, self.compensation = name, action, compensation


def run_saga(steps: list[SagaStep]):
    completed = []
    try:
        for step in steps:
            print(f"[saga] executing {step.name}")
            step.action()
            completed.append(step)
    except Exception as e:
        print(f"[saga] failure at step, rolling back: {e}")
        for step in reversed(completed):
            print(f"[saga] compensating {step.name}")
            step.compensation()
        raise


if __name__ == "__main__":
    def fail():
        raise RuntimeError("service indisponible")

    steps = [
        SagaStep("reserve-inventory", lambda: print("  stock reserve"), lambda: print("  stock libere")),
        SagaStep("charge-payment", lambda: print("  paiement debite"), lambda: print("  paiement rembourse")),
        SagaStep("ship-order", fail, lambda: print("  rien a compenser (jamais execute)")),
    ]
    try:
        run_saga(steps)
    except RuntimeError:
        print("[saga] transaction distribuee annulee proprement")
