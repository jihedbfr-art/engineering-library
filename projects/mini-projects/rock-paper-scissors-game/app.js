const CHOICES = ["pierre", "feuille", "ciseaux"];
const BEATS = { pierre: "ciseaux", feuille: "pierre", ciseaux: "feuille" };

let score = { player: 0, cpu: 0 };

function play(playerChoice) {
  const cpuChoice = CHOICES[Math.floor(Math.random() * 3)];
  let outcome;
  if (playerChoice === cpuChoice) {
    outcome = "Egalite";
  } else if (BEATS[playerChoice] === cpuChoice) {
    outcome = "Vous gagnez !";
    score.player++;
  } else {
    outcome = "La machine gagne !";
    score.cpu++;
  }
  document.getElementById("result").textContent =
    `Vous: ${playerChoice} vs Machine: ${cpuChoice} -> ${outcome}`;
  document.getElementById("score").textContent = `Vous: ${score.player} | Machine: ${score.cpu}`;
}

document.querySelectorAll("#choices button").forEach((btn) => {
  btn.addEventListener("click", () => play(btn.dataset.choice));
});
