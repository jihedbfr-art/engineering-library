DNA_TO_RNA = {"G": "C", "C": "G", "T": "A", "A": "U"}


def transcribe(dna: str) -> str:
    return "".join(DNA_TO_RNA[b] for b in dna.upper())


if __name__ == "__main__":
    dna = input("Brin ADN : ")
    print(transcribe(dna))
