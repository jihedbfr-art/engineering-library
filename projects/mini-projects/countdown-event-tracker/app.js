const KEY = "countdown-events";

function load() {
  return JSON.parse(localStorage.getItem(KEY) || "[]");
}

function save(events) {
  localStorage.setItem(KEY, JSON.stringify(events));
}

function daysUntil(dateStr) {
  const diff = new Date(dateStr) - new Date();
  return Math.ceil(diff / (1000 * 60 * 60 * 24));
}

function render() {
  const events = load().sort((a, b) => new Date(a.date) - new Date(b.date));
  document.getElementById("events").innerHTML = events.map((e) => {
    const days = daysUntil(e.date);
    const label = days > 0 ? `dans ${days} jour(s)` : days === 0 ? "aujourd'hui !" : `il y a ${-days} jour(s)`;
    return `<div class="event"><h3>${e.name}</h3><p>${e.date} — ${label}</p></div>`;
  }).join("") || "<p>Aucun evenement.</p>";
}

document.getElementById("add").addEventListener("click", () => {
  const name = document.getElementById("name").value.trim();
  const date = document.getElementById("date").value;
  if (!name || !date) return;
  const events = load();
  events.push({ name, date });
  save(events);
  document.getElementById("name").value = "";
  render();
});

render();
setInterval(render, 60000);
