const WORK_SECONDS = 25 * 60;
const BREAK_SECONDS = 5 * 60;

let secondsLeft = WORK_SECONDS;
let isWork = true;
let isRunning = false;
let intervalId = null;
let sessionCount = 0;

const timeDisplay = document.getElementById("timeDisplay");
const modeLabel = document.getElementById("modeLabel");
const startBtn = document.getElementById("startBtn");
const pauseBtn = document.getElementById("pauseBtn");
const resetBtn = document.getElementById("resetBtn");
const sessionCountEl = document.getElementById("sessionCount");

function formatTime(seconds) {
  const m = Math.floor(seconds / 60).toString().padStart(2, "0");
  const s = (seconds % 60).toString().padStart(2, "0");
  return `${m}:${s}`;
}

function render() {
  timeDisplay.textContent = formatTime(secondsLeft);
  modeLabel.textContent = isWork ? "Travail" : "Pause";
  sessionCountEl.textContent = sessionCount;
  startBtn.classList.toggle("hidden", isRunning);
  pauseBtn.classList.toggle("hidden", !isRunning);
}

function tick() {
  secondsLeft--;
  if (secondsLeft <= 0) {
    playBeep();
    if (isWork) sessionCount++;
    isWork = !isWork;
    secondsLeft = isWork ? WORK_SECONDS : BREAK_SECONDS;
  }
  render();
}

function start() {
  if (isRunning) return;
  isRunning = true;
  intervalId = setInterval(tick, 1000);
  render();
}

function pause() {
  isRunning = false;
  clearInterval(intervalId);
  render();
}

function reset() {
  pause();
  isWork = true;
  secondsLeft = WORK_SECONDS;
  render();
}

// Notification sonore simple via Web Audio API (pas de fichier audio externe)
function playBeep() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.type = "sine";
    osc.frequency.value = 880;
    gain.gain.setValueAtTime(0.2, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.6);
    osc.start();
    osc.stop(ctx.currentTime + 0.6);
  } catch (e) {
    // Web Audio indisponible : pas bloquant, le changement visuel suffit.
  }
}

startBtn.addEventListener("click", start);
pauseBtn.addEventListener("click", pause);
resetBtn.addEventListener("click", reset);

render();
