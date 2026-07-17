const HOLES = 9;
let score = 0, timeLeft = 30, activeHole = -1, gameInterval, moleInterval;

function render() {
  const grid = document.getElementById("grid");
  grid.innerHTML = "";
  for (let i = 0; i < HOLES; i++) {
    const div = document.createElement("div");
    div.className = "hole" + (i === activeHole ? " up" : "");
    div.textContent = i === activeHole ? "🐹" : "";
    div.onclick = () => whack(i);
    grid.appendChild(div);
  }
}

function whack(i) {
  if (i === activeHole) {
    score++;
    document.getElementById("score").textContent = score;
    activeHole = -1;
    render();
  }
}

function popMole() {
  activeHole = Math.floor(Math.random() * HOLES);
  render();
}

function startGame() {
  score = 0;
  timeLeft = 30;
  document.getElementById("score").textContent = score;
  document.getElementById("time").textContent = timeLeft;
  clearInterval(gameInterval);
  clearInterval(moleInterval);

  moleInterval = setInterval(popMole, 800);
  gameInterval = setInterval(() => {
    timeLeft--;
    document.getElementById("time").textContent = timeLeft;
    if (timeLeft <= 0) {
      clearInterval(gameInterval);
      clearInterval(moleInterval);
      activeHole = -1;
      render();
      alert(`Termine ! Score final : ${score}`);
    }
  }, 1000);
}

document.getElementById("start").addEventListener("click", startGame);
render();
