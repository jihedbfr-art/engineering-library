const STORAGE_KEY = 'recipe-book-app.recipes';
let recipes = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
let searchTerm = '';

const form = document.getElementById('recipe-form');
const list = document.getElementById('recipe-list');
const searchInput = document.getElementById('search');

function save() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(recipes));
}

function matchesSearch(recipe, term) {
  if (!term) return true;
  const t = term.toLowerCase();
  if (recipe.title.toLowerCase().includes(t)) return true;
  return recipe.ingredients.some(i => i.toLowerCase().includes(t));
}

function render() {
  list.innerHTML = '';
  const filtered = recipes.filter(r => matchesSearch(r, searchTerm));

  if (filtered.length === 0) {
    list.innerHTML = '<p style="color:#888;">Aucune recette trouvée.</p>';
    return;
  }

  filtered.forEach(recipe => {
    const card = document.createElement('div');
    card.className = 'recipe-card';

    const ingredientsHtml = recipe.ingredients.map(i => `<li>${escapeHtml(i)}</li>`).join('');
    const stepsHtml = recipe.steps.map(s => `<li>${escapeHtml(s)}</li>`).join('');

    card.innerHTML = `
      <h3>${escapeHtml(recipe.title)}</h3>
      <h4>Ingrédients</h4>
      <ul>${ingredientsHtml}</ul>
      <h4>Étapes</h4>
      <ol>${stepsHtml}</ol>
      <div class="actions"><button data-id="${recipe.id}">Supprimer</button></div>
    `;

    card.querySelector('button').addEventListener('click', () => {
      recipes = recipes.filter(r => r.id !== recipe.id);
      save();
      render();
    });

    list.appendChild(card);
  });
}

function escapeHtml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

form.addEventListener('submit', (e) => {
  e.preventDefault();
  const title = document.getElementById('title').value.trim();
  const ingredients = document.getElementById('ingredients').value
    .split('\n').map(s => s.trim()).filter(Boolean);
  const steps = document.getElementById('steps').value
    .split('\n').map(s => s.trim()).filter(Boolean);
  if (!title || ingredients.length === 0 || steps.length === 0) return;

  recipes.push({ id: Date.now(), title, ingredients, steps });
  form.reset();
  save();
  render();
});

searchInput.addEventListener('input', () => {
  searchTerm = searchInput.value.trim();
  render();
});

render();
