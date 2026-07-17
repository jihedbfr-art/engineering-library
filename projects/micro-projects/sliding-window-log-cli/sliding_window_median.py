import argparse
from collections import deque
from statistics import median


def sliding_medians(values: list[float], window: int) -> list[float]:
    dq = deque(maxlen=window)
    result = []
    for v in values:
        dq.append(v)
        if len(dq) == window:
            result.append(median(dq))
    return result


def main():
    parser = argparse.ArgumentParser(description="Mediane glissante sur une serie de valeurs")
    parser.add_argument("values", type=float, nargs="+")
    parser.add_argument("-w", "--window", type=int, default=3)
    args = parser.parse_args()
    print(sliding_medians(args.values, args.window))


if __name__ == "__main__":
    main()
