import argparse


def binary_search(arr: list[int], target: int) -> int:
    lo, hi = 0, len(arr) - 1
    while lo <= hi:
        mid = (lo + hi) // 2
        if arr[mid] == target:
            return mid
        if arr[mid] < target:
            lo = mid + 1
        else:
            hi = mid - 1
    return -1


def main():
    parser = argparse.ArgumentParser(description="Recherche dichotomique dans une liste triee")
    parser.add_argument("target", type=int)
    parser.add_argument("values", type=int, nargs="+")
    args = parser.parse_args()

    arr = sorted(args.values)
    idx = binary_search(arr, args.target)
    print(f"Liste triee: {arr}")
    print(f"{args.target} trouve a l'index {idx}" if idx >= 0 else f"{args.target} absent")


if __name__ == "__main__":
    main()
