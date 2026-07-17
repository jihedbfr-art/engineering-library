const KEY = "live-vote-counts";

function load() {
  return JSON.parse(localStorage.getItem(KEY) || JSON.stringify({ Java: 0, Python: 0, "Node.js": 0, Go: 0 }));
}

function save(votes) {
  localStorage.setItem(KEY, JSON.stringify(votes));
}

function render() {
  const votes = load();
  const total = Object.values(votes).reduce((a, b) => a + b, 0) || 1;

  document.getElementById("options").innerHTML = Object.entries(votes).map(([name, count]) => `
    <div class="option">
      <div>${name} (${count})</div>
      <div class="bar-bg"><div class="bar" style="width:${(count / total) * 100}%">${Math.round((count / total) * 100)}%</div></div>
      <button data-name="${name}">Voter</button>
    </div>
  `).join("");

  document.getElementById("total").textContent = `${total} vote(s) au total`;

  document.querySelectorAll("button[data-name]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const votes = load();
      votes[btn.dataset.name]++;
      save(votes);
      render();
    });
  });
}

render();
