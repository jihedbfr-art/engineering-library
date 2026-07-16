const DAYS = [
  ["monday", "Lundi"], ["tuesday", "Mardi"], ["wednesday", "Mercredi"],
  ["thursday", "Jeudi"], ["friday", "Vendredi"], ["saturday", "Samedi"], ["sunday", "Dimanche"]
];
const MEALS = [["breakfast", "Petit-déj"], ["lunch", "Déjeuner"], ["dinner", "Dîner"]];

let recipes = [];
let planning = {};
let editingId = null;

async function api(path, options) {
  const res = await fetch(path, options);
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error((data && data.error) || `HTTP ${res.status}`);
  return data;
}

function toast(msg) {
  const el = document.getElementById("toast");
  el.textContent = msg;
  el.classList.add("show");
  setTimeout(() => el.classList.remove("show"), 2000);
}

// ---------- Tabs ----------
document.querySelectorAll(".tab-btn").forEach(btn => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
    document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById("tab-" + btn.dataset.tab).classList.add("active");
    if (btn.dataset.tab === "shopping") loadShoppingList();
  });
});

// ---------- Recipes ----------
async function loadRecipes() {
  recipes = await api("/api/recipes");
  renderRecipes();
  renderPlanningGrid();
}

function renderRecipes() {
  const list = document.getElementById("recipes-list");
  list.innerHTML = "";
  recipes.forEach(r => {
    const li = document.createElement("li");
    li.className = "recipe-card";
    const ingredientsText = (r.ingredients || [])
      .map(i => `${i.quantity} ${i.unit} ${i.name}`.trim())
      .join(", ");
    li.innerHTML = `
      <h3>${escapeHtml(r.name)}</h3>
      <div class="ingredients">${escapeHtml(ingredientsText) || "Aucun ingrédient"}</div>
      <div class="actions">
        <button class="edit-btn secondary">Modifier</button>
        <button class="del-btn danger">Supprimer</button>
      </div>
    `;
    li.querySelector(".edit-btn").addEventListener("click", () => startEdit(r));
    li.querySelector(".del-btn").addEventListener("click", () => deleteRecipe(r.id));
    list.appendChild(li);
  });
}

function escapeHtml(s) {
  return (s || "").replace(/[&<>"']/g, c => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  }[c]));
}

function addIngredientRow(name = "", quantity = "", unit = "") {
  const row = document.createElement("div");
  row.className = "ingredient-row";
  row.innerHTML = `
    <input type="text" class="ing-name" placeholder="Ingrédient" value="${escapeHtml(name)}">
    <input type="number" step="any" class="qty" placeholder="Qté" value="${quantity}">
    <input type="text" class="unit" placeholder="unité" value="${escapeHtml(unit)}">
    <button type="button" class="remove-ing danger">x</button>
  `;
  row.querySelector(".remove-ing").addEventListener("click", () => row.remove());
  document.getElementById("ingredients-list").appendChild(row);
}

document.getElementById("add-ingredient").addEventListener("click", () => addIngredientRow());

function startEdit(recipe) {
  editingId = recipe.id;
  document.getElementById("recipe-id").value = recipe.id;
  document.getElementById("recipe-name").value = recipe.name;
  document.getElementById("recipe-instructions").value = recipe.instructions || "";
  document.getElementById("ingredients-list").innerHTML = "";
  (recipe.ingredients || []).forEach(i => addIngredientRow(i.name, i.quantity, i.unit));
  window.scrollTo({ top: 0, behavior: "smooth" });
}

document.getElementById("cancel-edit").addEventListener("click", resetForm);

function resetForm() {
  editingId = null;
  document.getElementById("recipe-form").reset();
  document.getElementById("ingredients-list").innerHTML = "";
}

document.getElementById("recipe-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const name = document.getElementById("recipe-name").value.trim();
  const instructions = document.getElementById("recipe-instructions").value.trim();
  const ingredients = Array.from(document.querySelectorAll(".ingredient-row")).map(row => ({
    name: row.querySelector(".ing-name").value.trim(),
    quantity: parseFloat(row.querySelector(".qty").value) || 0,
    unit: row.querySelector(".unit").value.trim()
  })).filter(i => i.name);

  try {
    if (editingId) {
      await api(`/api/recipes/${editingId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, instructions, ingredients })
      });
      toast("Recette mise à jour");
    } else {
      await api("/api/recipes", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, instructions, ingredients })
      });
      toast("Recette créée");
    }
    resetForm();
    await loadRecipes();
  } catch (err) {
    toast("Erreur: " + err.message);
  }
});

async function deleteRecipe(id) {
  if (!confirm("Supprimer cette recette ?")) return;
  try {
    await api(`/api/recipes/${id}`, { method: "DELETE" });
    toast("Recette supprimée");
    await loadRecipes();
    await loadPlanning();
  } catch (err) {
    toast("Erreur: " + err.message);
  }
}

// ---------- Planning ----------
async function loadPlanning() {
  planning = await api("/api/planning");
  renderPlanningGrid();
}

function renderPlanningGrid() {
  const grid = document.getElementById("planning-grid");
  grid.innerHTML = "";
  grid.appendChild(el("div", ""));
  MEALS.forEach(([, label]) => grid.appendChild(el("div", label, "header-cell")));

  DAYS.forEach(([dayKey, dayLabel]) => {
    grid.appendChild(el("div", dayLabel, "day-label"));
    MEALS.forEach(([mealKey]) => {
      const select = document.createElement("select");
      const emptyOpt = document.createElement("option");
      emptyOpt.value = "";
      emptyOpt.textContent = "-- aucune --";
      select.appendChild(emptyOpt);
      recipes.forEach(r => {
        const opt = document.createElement("option");
        opt.value = r.id;
        opt.textContent = r.name;
        select.appendChild(opt);
      });
      const current = planning[dayKey] && planning[dayKey][mealKey];
      select.value = current || "";
      select.addEventListener("change", () => updatePlanningSlot(dayKey, mealKey, select.value || null));
      grid.appendChild(select);
    });
  });
}

function el(tag, text, cls) {
  const e = document.createElement(tag);
  e.textContent = text;
  if (cls) e.className = cls;
  return e;
}

async function updatePlanningSlot(day, meal, recipeId) {
  const body = {};
  body[day] = { [meal]: recipeId };
  try {
    planning = await api("/api/planning", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });
    toast("Planning mis à jour");
  } catch (err) {
    toast("Erreur: " + err.message);
    await loadPlanning();
  }
}

// ---------- Shopping list ----------
async function loadShoppingList() {
  const list = await api("/api/shopping-list");
  const ul = document.getElementById("shopping-list");
  ul.innerHTML = "";
  if (list.length === 0) {
    ul.innerHTML = "<li>Aucun ingrédient planifié cette semaine.</li>";
    return;
  }
  list.forEach(item => {
    const li = document.createElement("li");
    li.innerHTML = `<span>${escapeHtml(item.name)}</span><span>${item.quantity} ${escapeHtml(item.unit)}</span>`;
    ul.appendChild(li);
  });
}

(async function init() {
  await loadRecipes();
  await loadPlanning();
})();
