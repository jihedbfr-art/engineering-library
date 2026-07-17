const SIZE = 8, MINES = 10;
let board, revealed, gameOver;

function newGame() {
  board = Array.from({ length: SIZE }, () => Array(SIZE).fill(0));
  revealed = Array.from({ length: SIZE }, () => Array(SIZE).fill(false));
  gameOver = false;

  let placed = 0;
  while (placed < MINES) {
    const r = Math.floor(Math.random() * SIZE);
    const c = Math.floor(Math.random() * SIZE);
    if (board[r][c] !== "M") {
      board[r][c] = "M";
      placed++;
    }
  }
  for (let r = 0; r < SIZE; r++) {
    for (let c = 0; c < SIZE; c++) {
      if (board[r][c] === "M") continue;
      let count = 0;
      for (let dr = -1; dr <= 1; dr++)
        for (let dc = -1; dc <= 1; dc++)
          if (board[r + dr]?.[c + dc] === "M") count++;
      board[r][c] = count;
    }
  }
  render();
}

function reveal(r, c) {
  if (gameOver || revealed[r][c]) return;
  revealed[r][c] = true;
  if (board[r][c] === "M") {
    gameOver = true;
    document.getElementById("status").textContent = "Boom ! Partie perdue.";
  } else if (board[r][c] === 0) {
    for (let dr = -1; dr <= 1; dr++)
      for (let dc = -1; dc <= 1; dc++)
        if (board[r + dr]?.[c + dc] !== undefined) reveal(r + dr, c + dc);
  }
  render();
}

function render() {
  const grid = document.getElementById("grid");
  grid.innerHTML = "";
  for (let r = 0; r < SIZE; r++) {
    for (let c = 0; c < SIZE; c++) {
      const div = document.createElement("div");
      div.className = "cell" + (revealed[r][c] ? " revealed" : "") + (revealed[r][c] && board[r][c] === "M" ? " mine" : "");
      if (revealed[r][c]) div.textContent = board[r][c] === "M" ? "💣" : board[r][c] || "";
      div.onclick = () => reveal(r, c);
      grid.appendChild(div);
    }
  }
}

newGame();
