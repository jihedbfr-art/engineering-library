const SAMPLES = [
  "Le rapide renard brun saute par dessus le chien paresseux.",
  "La programmation est l art de resoudre des problemes avec de la logique.",
  "Un microservice bien concu fait une seule chose et la fait bien.",
];

let startTime = null;
let sample = "";

const promptEl = document.getElementById("prompt-text");
const inputEl = document.getElementById("input-area");
const timeEl = document.getElementById("time");
const wpmEl = document.getElementById("wpm");
const accuracyEl = document.getElementById("accuracy");

function newRound() {
  sample = SAMPLES[Math.floor(Math.random() * SAMPLES.length)];
  promptEl.textContent = sample;
  inputEl.value = "";
  startTime = null;
  timeEl.textContent = "0";
  wpmEl.textContent = "0";
  accuracyEl.textContent = "100";
  inputEl.focus();
}

function accuracy(typed, target) {
  let correct = 0;
  for (let i = 0; i < typed.length; i++) if (typed[i] === target[i]) correct++;
  return typed.length ? Math.round((correct / typed.length) * 100) : 100;
}

inputEl.addEventListener("input", () => {
  if (!startTime) startTime = Date.now();
  const elapsedSec = (Date.now() - startTime) / 1000;
  const typed = inputEl.value;
  const words = typed.trim().split(/\s+/).filter(Boolean).length;
  const wpm = elapsedSec > 0 ? Math.round((words / elapsedSec) * 60) : 0;

  timeEl.textContent = elapsedSec.toFixed(1);
  wpmEl.textContent = wpm;
  accuracyEl.textContent = accuracy(typed, sample);
});

document.getElementById("restart").addEventListener("click", newRound);
newRound();
