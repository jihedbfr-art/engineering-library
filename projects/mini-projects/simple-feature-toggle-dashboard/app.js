const FLAGS_KEY = "feature-flags-state";

function loadFlags() {
  return JSON.parse(localStorage.getItem(FLAGS_KEY) || JSON.stringify({
    "nouveau-checkout": true,
    "mode-sombre": false,
    "recommandations-ia": false,
    "paiement-instantane": true,
  }));
}

function saveFlags(flags) {
  localStorage.setItem(FLAGS_KEY, JSON.stringify(flags));
}

function render() {
  const flags = loadFlags();
  const container = document.getElementById("flags");
  container.innerHTML = Object.entries(flags).map(([name, enabled]) => `
    <div class="flag">
      <span>${name}</span>
      <div class="switch ${enabled ? "on" : ""}" data-name="${name}"></div>
    </div>
  `).join("");

  container.querySelectorAll(".switch").forEach((el) => {
    el.addEventListener("click", () => {
      const flags = loadFlags();
      flags[el.dataset.name] = !flags[el.dataset.name];
      saveFlags(flags);
      render();
    });
  });
}

render();
