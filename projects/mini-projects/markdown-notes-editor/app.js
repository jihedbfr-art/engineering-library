const STORAGE_KEY = "markdown-notes-editor:content";

const input = document.getElementById("markdownInput");
const preview = document.getElementById("preview");
const saveStatus = document.getElementById("saveStatus");

const DEFAULT_CONTENT = `# Bienvenue

Ceci est un éditeur **Markdown** avec *preview* en temps réel.

- Tapez du texte à gauche
- Le rendu HTML apparaît à droite
- Essayez un [lien](https://example.com)
`;

let saveTimeout = null;

function loadContent() {
  return localStorage.getItem(STORAGE_KEY) ?? DEFAULT_CONTENT;
}

function render() {
  preview.innerHTML = renderMarkdown(input.value);
}

function scheduleSave() {
  saveStatus.textContent = "Modification...";
  clearTimeout(saveTimeout);
  saveTimeout = setTimeout(() => {
    localStorage.setItem(STORAGE_KEY, input.value);
    saveStatus.textContent = "Enregistré";
  }, 400);
}

input.value = loadContent();
render();

input.addEventListener("input", () => {
  render();
  scheduleSave();
});
