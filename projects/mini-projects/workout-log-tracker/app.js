const KEY = "workout-log";

function load() {
  return JSON.parse(localStorage.getItem(KEY) || "[]");
}

function save(entries) {
  localStorage.setItem(KEY, JSON.stringify(entries));
}

function render() {
  const entries = load();
  const tbody = document.querySelector("#log tbody");
  tbody.innerHTML = entries.slice().reverse().map((e) => `
    <tr><td>${e.date}</td><td>${e.exercise}</td><td>${e.weight}kg</td><td>${e.reps}</td><td>${e.weight * e.reps}kg</td></tr>
  `).join("");
}

document.getElementById("add").addEventListener("click", () => {
  const exercise = document.getElementById("exercise").value.trim();
  const weight = parseFloat(document.getElementById("weight").value);
  const reps = parseInt(document.getElementById("reps").value, 10);
  if (!exercise || !weight || !reps) return;

  const entries = load();
  entries.push({ date: new Date().toISOString().slice(0, 10), exercise, weight, reps });
  save(entries);
  render();
});

render();
