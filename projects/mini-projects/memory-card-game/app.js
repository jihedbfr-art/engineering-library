const EMOJIS = ["🐶", "🐱", "🦊", "🐼", "🐸", "🦁", "🐵", "🐨"];

const board = document.getElementById("board");
const movesValue = document.getElementById("movesValue");
const matchesValue = document.getElementById("matchesValue");
const totalPairs = document.getElementById("totalPairs");
const winMessage = document.getElementById("winMessage");
const restartBtn = document.getElementById("restartBtn");

let cards = [];
let flipped = [];
let matchedCount = 0;
let moves = 0;
let lockBoard = false;

function shuffle(array) {
  const arr = [...array];
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr;
}

function init() {
  const deck = shuffle([...EMOJIS, ...EMOJIS]);
  cards = deck.map((emoji, idx) => ({ id: idx, emoji, isFlipped: false, isMatched: false }));
  flipped = [];
  matchedCount = 0;
  moves = 0;
  lockBoard = false;
  totalPairs.textContent = EMOJIS.length;
  movesValue.textContent = moves;
  matchesValue.textContent = matchedCount;
  winMessage.classList.add("hidden");
  render();
}

function render() {
  board.innerHTML = "";
  cards.forEach(card => {
    const div = document.createElement("div");
    div.className = "card" + (card.isFlipped ? " flipped" : "") + (card.isMatched ? " matched" : "");
    div.textContent = (card.isFlipped || card.isMatched) ? card.emoji : "❓";
    div.addEventListener("click", () => flipCard(card.id));
    board.appendChild(div);
  });
}

function flipCard(id) {
  if (lockBoard) return;
  const card = cards.find(c => c.id === id);
  if (!card || card.isFlipped || card.isMatched) return;

  card.isFlipped = true;
  flipped.push(card);
  render();

  if (flipped.length === 2) {
    moves++;
    movesValue.textContent = moves;
    lockBoard = true;

    const [first, second] = flipped;
    if (first.emoji === second.emoji) {
      first.isMatched = true;
      second.isMatched = true;
      matchedCount++;
      matchesValue.textContent = matchedCount;
      flipped = [];
      lockBoard = false;
      render();
      checkWin();
    } else {
      setTimeout(() => {
        first.isFlipped = false;
        second.isFlipped = false;
        flipped = [];
        lockBoard = false;
        render();
      }, 700);
    }
  }
}

function checkWin() {
  if (matchedCount === EMOJIS.length) {
    winMessage.classList.remove("hidden");
  }
}

restartBtn.addEventListener("click", init);

init();
