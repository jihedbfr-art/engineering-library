const KEY = "code-snippets";

function load() {
  return JSON.parse(localStorage.getItem(KEY) || "[]");
}

function save(snippets) {
  localStorage.setItem(KEY, JSON.stringify(snippets));
}

function render() {
  const search = document.getElementById("search").value.toLowerCase();
  const snippets = load().filter((s) => s.title.toLowerCase().includes(search) || s.code.toLowerCase().includes(search));

  document.getElementById("list").innerHTML = snippets.map((s, i) => `
    <div class="snippet">
      <strong>${s.title}</strong> <em>(${s.lang})</em>
      <pre>${s.code.replace(/</g, "&lt;")}</pre>
      <button data-i="${i}" class="delete">Supprimer</button>
    </div>
  `).join("") || "<p>Aucun snippet.</p>";

  document.querySelectorAll(".delete").forEach((btn) => {
    btn.addEventListener("click", () => {
      const all = load();
      all.splice(btn.dataset.i, 1);
      save(all);
      render();
    });
  });
}

document.getElementById("add").addEventListener("click", () => {
  const title = document.getElementById("title").value.trim();
  const code = document.getElementById("code").value.trim();
  const lang = document.getElementById("lang").value;
  if (!title || !code) return;

  const snippets = load();
  snippets.push({ title, code, lang });
  save(snippets);
  document.getElementById("title").value = "";
  document.getElementById("code").value = "";
  render();
});

document.getElementById("search").addEventListener("input", render);
render();
