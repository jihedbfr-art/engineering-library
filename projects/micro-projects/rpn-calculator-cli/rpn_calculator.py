import argparse
import operator

OPS = {"+": operator.add, "-": operator.sub, "*": operator.mul, "/": operator.truediv}


def evaluate(tokens: list[str]) -> float:
    stack = []
    for tok in tokens:
        if tok in OPS:
            b, a = stack.pop(), stack.pop()
            stack.append(OPS[tok](a, b))
        else:
            stack.append(float(tok))
    return stack[0]


def main():
    parser = argparse.ArgumentParser(description="Calculatrice en notation polonaise inversee (RPN)")
    parser.add_argument("tokens", nargs="+", help="ex: 3 4 + 2 *")
    args = parser.parse_args()
    print(evaluate(args.tokens))


if __name__ == "__main__":
    main()
