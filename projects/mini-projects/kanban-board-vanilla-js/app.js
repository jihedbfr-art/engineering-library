const COLUMNS = ["A faire", "En cours", "Termine"];
const KEY = "kanban-vanilla-cards";

function load() {
  return JSON.parse(localStorage.getItem(KEY) || JSON.stringify({
    "A faire": ["Ecrire les specs", "Setup CI"],
    "En cours": ["Implementer l'auth"],
    "Termine": ["Init du repo"],
  }));
}

function save(data) {
  localStorage.setItem(KEY, JSON.stringify(data));
}

function render() {
  const data = load();
  const board = document.getElementById("board");
  board.innerHTML = COLUMNS.map((col) => `
    <div class="column" data-col="${col}">
      <h3>${col}</h3>
      ${data[col].map((card, i) => `<div class="card" draggable="true" data-col="${col}" data-i="${i}">${card}</div>`).join("")}
      <input placeholder="+ nouvelle carte" data-add="${col}">
    </div>
  `).join("");

  board.querySelectorAll(".card").forEach((card) => {
    card.addEventListener("dragstart", (e) => {
      e.dataTransfer.setData("text", JSON.stringify({ col: card.dataset.col, i: card.dataset.i }));
    });
  });

  board.querySelectorAll(".column").forEach((col) => {
    col.addEventListener("dragover", (e) => { e.preventDefault(); col.classList.add("dragover"); });
    col.addEventListener("dragleave", () => col.classList.remove("dragover"));
    col.addEventListener("drop", (e) => {
      e.preventDefault();
      col.classList.remove("dragover");
      const { col: fromCol, i } = JSON.parse(e.dataTransfer.getData("text"));
      const data = load();
      const [card] = data[fromCol].splice(i, 1);
      data[col.dataset.col].push(card);
      save(data);
      render();
    });
  });

  board.querySelectorAll("input[data-add]").forEach((input) => {
    input.addEventListener("keydown", (e) => {
      if (e.key === "Enter" && input.value.trim()) {
        const data = load();
        data[input.dataset.add].push(input.value.trim());
        save(data);
        render();
      }
    });
  });
}

render();
