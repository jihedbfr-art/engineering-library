const QUOTES = [
  "Le code simple est le code qui survit aux refactorings.",
  "Un bug non teste est un bug en production qui attend son heure.",
  "La documentation d'aujourd'hui, c'est le temps gagne de demain.",
];

function updateClock() {
  document.getElementById("clock").textContent = new Date().toLocaleTimeString();
}

function loadTodos() {
  return JSON.parse(localStorage.getItem("dashboard-todos") || "[]");
}

function saveTodos(todos) {
  localStorage.setItem("dashboard-todos", JSON.stringify(todos));
}

function renderTodos() {
  const todos = loadTodos();
  document.getElementById("todo-list").innerHTML = todos.map((t, i) =>
    `<li>${t} <button data-i="${i}" class="del">x</button></li>`
  ).join("");
  document.querySelectorAll(".del").forEach((btn) => {
    btn.addEventListener("click", () => {
      const todos = loadTodos();
      todos.splice(btn.dataset.i, 1);
      saveTodos(todos);
      renderTodos();
    });
  });
}

document.getElementById("todo-input").addEventListener("keydown", (e) => {
  if (e.key === "Enter" && e.target.value.trim()) {
    const todos = loadTodos();
    todos.push(e.target.value.trim());
    saveTodos(todos);
    e.target.value = "";
    renderTodos();
  }
});

document.getElementById("quote").textContent = QUOTES[Math.floor(Math.random() * QUOTES.length)];
updateClock();
setInterval(updateClock, 1000);
renderTodos();
