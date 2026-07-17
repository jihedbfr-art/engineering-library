class OrderStateMachine:
    TRANSITIONS = {
        "CREATED": {"PAY": "PAID", "CANCEL": "CANCELLED"},
        "PAID": {"SHIP": "SHIPPED", "REFUND": "REFUNDED"},
        "SHIPPED": {"DELIVER": "DELIVERED"},
        "DELIVERED": {},
        "CANCELLED": {},
        "REFUNDED": {},
    }

    def __init__(self):
        self.state = "CREATED"

    def apply(self, event: str):
        next_state = self.TRANSITIONS[self.state].get(event)
        if next_state is None:
            raise ValueError(f"Transition '{event}' invalide depuis l'etat '{self.state}'")
        self.state = next_state


if __name__ == "__main__":
    order = OrderStateMachine()
    for event in ["PAY", "SHIP", "DELIVER"]:
        order.apply(event)
        print(f"{event} -> {order.state}")
