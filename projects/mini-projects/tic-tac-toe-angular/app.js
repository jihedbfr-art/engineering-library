const WIN_LINES = [
  [0, 1, 2], [3, 4, 5], [6, 7, 8], // lignes
  [0, 3, 6], [1, 4, 7], [2, 5, 8], // colonnes
  [0, 4, 8], [2, 4, 6]             // diagonales
];

let board = Array(9).fill(null);
let currentPlayer = "X";
let gameOver = false;

const boardEl = document.getElementById("board");
const statusText = document.getElementById("statusText");
const resetBtn = document.getElementById("resetBtn");

function checkWinner() {
  for (const line of WIN_LINES) {
    const [a, b, c] = line;
    if (board[a] && board[a] === board[b] && board[a] === board[c]) {
      return { winner: board[a], line };
    }
  }
  if (board.every(cell => cell !== null)) {
    return { winner: "draw", line: [] };
  }
  return null;
}

function render(winInfo) {
  boardEl.innerHTML = "";
  board.forEach((value, idx) => {
    const btn = document.createElement("button");
    btn.className = "cell";
    btn.textContent = value || "";
    if (winInfo && winInfo.line.includes(idx)) btn.classList.add("win");
    if (value || gameOver) btn.disabled = true;
    btn.addEventListener("click", () => playMove(idx));
    boardEl.appendChild(btn);
  });

  if (winInfo && winInfo.winner === "draw") {
    statusText.textContent = "Égalité !";
  } else if (winInfo) {
    statusText.textContent = `Joueur ${winInfo.winner} a gagné !`;
  } else {
    statusText.textContent = `Au tour de ${currentPlayer}`;
  }
}

function playMove(idx) {
  if (gameOver || board[idx]) return;
  board[idx] = currentPlayer;

  const result = checkWinner();
  if (result) {
    gameOver = true;
    render(result);
    return;
  }

  currentPlayer = currentPlayer === "X" ? "O" : "X";
  render(null);
}

function reset() {
  board = Array(9).fill(null);
  currentPlayer = "X";
  gameOver = false;
  render(null);
}

resetBtn.addEventListener("click", reset);

render(null);
