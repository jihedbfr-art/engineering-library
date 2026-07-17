const WORDS = ["JAVASCRIPT", "MICROSERVICE", "KEYCLOAK", "TELECOM", "KUBERNETES"];
let word, guessed, wrong;

function newGame() {
  word = WORDS[Math.floor(Math.random() * WORDS.length)];
  guessed = new Set();
  wrong = 0;
  render();
}

function render() {
  document.getElementById("word").textContent = word
    .split("")
    .map((c) => (guessed.has(c) ? c : "_"))
    .join(" ");
  document.getElementById("tries").textContent = `Essais restants : ${6 - wrong}`;
  const kb = document.getElementById("keyboard");
  kb.innerHTML = "";
  "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("").forEach((letter) => {
    const btn = document.createElement("button");
    btn.textContent = letter;
    btn.disabled = guessed.has(letter) || wrong >= 6 || !word.includes("_") && false;
    btn.onclick = () => guess(letter);
    kb.appendChild(btn);
  });
  const msg = document.getElementById("message");
  if (word.split("").every((c) => guessed.has(c))) msg.textContent = "Gagne !";
  else if (wrong >= 6) msg.textContent = `Perdu ! Le mot etait ${word}`;
  else msg.textContent = "";
}

function guess(letter) {
  guessed.add(letter);
  if (!word.includes(letter)) wrong++;
  render();
}

newGame();
