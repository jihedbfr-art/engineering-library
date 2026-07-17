def check_winner(board: list[str]) -> str | None:
    lines = [(0,1,2),(3,4,5),(6,7,8),(0,3,6),(1,4,7),(2,5,8),(0,4,8),(2,4,6)]
    for a, b, c in lines:
        if board[a] != " " and board[a] == board[b] == board[c]:
            return board[a]
    return None


if __name__ == "__main__":
    board = list("XOXOXOOXX")
    winner = check_winner(board)
    print(f"Gagnant : {winner}" if winner else "Pas de gagnant")
