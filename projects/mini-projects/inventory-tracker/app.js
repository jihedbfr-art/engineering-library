const STORAGE_KEY = "inventory-tracker:items";

function loadItems() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];
  } catch {
    return [];
  }
}

function saveItems(items) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
}

let items = loadItems();
let nextId = items.reduce((max, i) => Math.max(max, i.id), 0) + 1;

const itemsBody = document.getElementById("itemsBody");
const emptyMessage = document.getElementById("emptyMessage");
const searchInput = document.getElementById("searchInput");
const lowOnlyCheckbox = document.getElementById("lowOnlyCheckbox");

function addItem(name, qty, threshold) {
  items.push({ id: nextId++, name, qty, threshold });
  saveItems(items);
  render();
}

function updateQty(id, delta) {
  const item = items.find(i => i.id === id);
  if (!item) return;
  item.qty = Math.max(0, item.qty + delta);
  saveItems(items);
  render();
}

function deleteItem(id) {
  items = items.filter(i => i.id !== id);
  saveItems(items);
  render();
}

function render() {
  const search = searchInput.value.trim().toLowerCase();
  const lowOnly = lowOnlyCheckbox.checked;

  let filtered = items.filter(i => i.name.toLowerCase().includes(search));
  if (lowOnly) {
    filtered = filtered.filter(i => i.qty < i.threshold);
  }

  itemsBody.innerHTML = "";
  filtered.forEach(item => {
    const isLow = item.qty < item.threshold;
    const tr = document.createElement("tr");
    if (isLow) tr.classList.add("low-stock");
    tr.innerHTML = `
      <td>${escapeHtml(item.name)}</td>
      <td>${item.qty}</td>
      <td>${item.threshold}</td>
      <td><span class="badge ${isLow ? 'low' : 'ok'}">${isLow ? 'Stock bas' : 'OK'}</span></td>
      <td class="actions">
        <button class="plus" data-id="${item.id}">+1</button>
        <button class="minus" data-id="${item.id}">-1</button>
        <button class="del" data-id="${item.id}">Supprimer</button>
      </td>
    `;
    itemsBody.appendChild(tr);
  });

  itemsBody.querySelectorAll(".plus").forEach(b => b.addEventListener("click", () => updateQty(Number(b.dataset.id), 1)));
  itemsBody.querySelectorAll(".minus").forEach(b => b.addEventListener("click", () => updateQty(Number(b.dataset.id), -1)));
  itemsBody.querySelectorAll(".del").forEach(b => b.addEventListener("click", () => deleteItem(Number(b.dataset.id))));

  emptyMessage.classList.toggle("hidden", filtered.length > 0);
}

function escapeHtml(s) {
  const div = document.createElement("div");
  div.textContent = s;
  return div.innerHTML;
}

document.getElementById("itemForm").addEventListener("submit", (e) => {
  e.preventDefault();
  const name = document.getElementById("nameInput").value.trim();
  const qty = parseInt(document.getElementById("qtyInput").value, 10);
  const threshold = parseInt(document.getElementById("thresholdInput").value, 10);
  if (!name || isNaN(qty) || isNaN(threshold)) return;
  addItem(name, qty, threshold);
  e.target.reset();
  document.getElementById("thresholdInput").value = 5;
});

searchInput.addEventListener("input", render);
lowOnlyCheckbox.addEventListener("change", render);

render();
