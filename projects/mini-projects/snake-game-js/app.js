const CELL_SIZE = 20;
const GRID_SIZE = 20; // 400 / 20
const TICK_MS = 120;

const canvas = document.getElementById("gameCanvas");
const ctx = canvas.getContext("2d");
const scoreValue = document.getElementById("scoreValue");
const overlay = document.getElementById("gameOverOverlay");
const restartBtn = document.getElementById("restartBtn");

let snake, direction, nextDirection, food, score, gameOver, intervalId;

function randomCell() {
  return {
    x: Math.floor(Math.random() * GRID_SIZE),
    y: Math.floor(Math.random() * GRID_SIZE)
  };
}

function placeFood() {
  let candidate;
  do {
    candidate = randomCell();
  } while (snake.some(seg => seg.x === candidate.x && seg.y === candidate.y));
  return candidate;
}

function init() {
  snake = [{ x: 10, y: 10 }, { x: 9, y: 10 }, { x: 8, y: 10 }];
  direction = { x: 1, y: 0 };
  nextDirection = { x: 1, y: 0 };
  food = placeFood();
  score = 0;
  gameOver = false;
  scoreValue.textContent = score;
  overlay.classList.add("hidden");

  if (intervalId) clearInterval(intervalId);
  intervalId = setInterval(tick, TICK_MS);
  draw();
}

function tick() {
  if (gameOver) return;

  direction = nextDirection;
  const head = { x: snake[0].x + direction.x, y: snake[0].y + direction.y };

  const hitsWall = head.x < 0 || head.x >= GRID_SIZE || head.y < 0 || head.y >= GRID_SIZE;
  const hitsSelf = snake.some(seg => seg.x === head.x && seg.y === head.y);

  if (hitsWall || hitsSelf) {
    endGame();
    return;
  }

  snake.unshift(head);

  if (head.x === food.x && head.y === food.y) {
    score++;
    scoreValue.textContent = score;
    food = placeFood();
  } else {
    snake.pop();
  }

  draw();
}

function endGame() {
  gameOver = true;
  clearInterval(intervalId);
  overlay.classList.remove("hidden");
}

function draw() {
  ctx.fillStyle = "#0d1117";
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  ctx.fillStyle = "#f85149";
  ctx.fillRect(food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE - 2, CELL_SIZE - 2);

  snake.forEach((seg, idx) => {
    ctx.fillStyle = idx === 0 ? "#7ee787" : "#2ea043";
    ctx.fillRect(seg.x * CELL_SIZE, seg.y * CELL_SIZE, CELL_SIZE - 2, CELL_SIZE - 2);
  });
}

document.addEventListener("keydown", (e) => {
  switch (e.key) {
    case "ArrowUp":
      if (direction.y === 0) nextDirection = { x: 0, y: -1 };
      break;
    case "ArrowDown":
      if (direction.y === 0) nextDirection = { x: 0, y: 1 };
      break;
    case "ArrowLeft":
      if (direction.x === 0) nextDirection = { x: -1, y: 0 };
      break;
    case "ArrowRight":
      if (direction.x === 0) nextDirection = { x: 1, y: 0 };
      break;
    case " ":
      if (gameOver) init();
      break;
  }
});

restartBtn.addEventListener("click", init);

init();
