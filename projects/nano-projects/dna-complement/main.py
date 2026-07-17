COMPLEMENT = {"A": "T", "T": "A", "C": "G", "G": "C"}


def dna_complement(strand: str) -> str:
    return "".join(COMPLEMENT[b] for b in strand.upper())


if __name__ == "__main__":
    strand = input("Brin ADN (ex: GATTACA) : ")
    print(dna_complement(strand))
