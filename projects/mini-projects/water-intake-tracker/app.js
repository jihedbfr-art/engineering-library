const GOAL_ML = 2000;
const KEY = "water-intake-" + new Date().toISOString().slice(0, 10);

function load() {
  return parseInt(localStorage.getItem(KEY) || "0", 10);
}

function save(ml) {
  localStorage.setItem(KEY, ml);
}

function render() {
  const total = load();
  document.getElementById("total").textContent = `${total} / ${GOAL_ML} ml`;
  document.getElementById("water").style.height = Math.min(100, (total / GOAL_ML) * 100) + "%";
}

document.querySelectorAll("button[data-ml]").forEach((btn) => {
  btn.addEventListener("click", () => {
    save(load() + parseInt(btn.dataset.ml, 10));
    render();
  });
});

document.getElementById("reset").addEventListener("click", () => {
  save(0);
  render();
});

render();
