import heapq
from collections import Counter


class Node:
    def __init__(self, char, freq, left=None, right=None):
        self.char, self.freq, self.left, self.right = char, freq, left, right

    def __lt__(self, other):
        return self.freq < other.freq


def build_tree(text: str) -> Node:
    heap = [Node(c, f) for c, f in Counter(text).items()]
    heapq.heapify(heap)
    while len(heap) > 1:
        a, b = heapq.heappop(heap), heapq.heappop(heap)
        heapq.heappush(heap, Node(None, a.freq + b.freq, a, b))
    return heap[0]


def build_codes(node: Node, prefix: str = "", codes: dict = None) -> dict:
    codes = codes if codes is not None else {}
    if node.char is not None:
        codes[node.char] = prefix or "0"
    else:
        build_codes(node.left, prefix + "0", codes)
        build_codes(node.right, prefix + "1", codes)
    return codes


if __name__ == "__main__":
    text = "abracadabra"
    codes = build_codes(build_tree(text))
    encoded = "".join(codes[c] for c in text)
    print("Codes :", codes)
    print(f"Original: {len(text) * 8} bits -> Encode: {len(encoded)} bits")
