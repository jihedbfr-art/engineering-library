const ROWS = 6, COLS = 7;
let board, currentPlayer, gameOver;

function newGame() {
  board = Array.from({ length: ROWS }, () => Array(COLS).fill(null));
  currentPlayer = "red";
  gameOver = false;
  updateStatus();
  render();
}

function drop(col) {
  if (gameOver) return;
  for (let row = ROWS - 1; row >= 0; row--) {
    if (!board[row][col]) {
      board[row][col] = currentPlayer;
      if (checkWin(row, col)) {
        gameOver = true;
        document.getElementById("status").textContent = `${currentPlayer === "red" ? "Rouge" : "Jaune"} gagne !`;
      } else {
        currentPlayer = currentPlayer === "red" ? "yellow" : "red";
        updateStatus();
      }
      render();
      return;
    }
  }
}

function checkWin(row, col) {
  const player = board[row][col];
  const directions = [[0, 1], [1, 0], [1, 1], [1, -1]];
  for (const [dr, dc] of directions) {
    let count = 1;
    for (const sign of [1, -1]) {
      let r = row + dr * sign, c = col + dc * sign;
      while (board[r]?.[c] === player) {
        count++;
        r += dr * sign;
        c += dc * sign;
      }
    }
    if (count >= 4) return true;
  }
  return false;
}

function updateStatus() {
  document.getElementById("status").textContent = `Tour du joueur ${currentPlayer === "red" ? "Rouge" : "Jaune"}`;
}

function render() {
  const grid = document.getElementById("grid");
  grid.innerHTML = "";
  for (let r = 0; r < ROWS; r++) {
    for (let c = 0; c < COLS; c++) {
      const div = document.createElement("div");
      div.className = "cell" + (board[r][c] ? " " + board[r][c] : "");
      div.onclick = () => drop(c);
      grid.appendChild(div);
    }
  }
}

document.getElementById("restart").addEventListener("click", newGame);
newGame();
