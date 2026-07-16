const STORAGE_KEY = "contact-manager-crud:contacts";

function loadContacts() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];
  } catch {
    return [];
  }
}

function saveContacts(contacts) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(contacts));
}

let contacts = loadContacts();
let nextId = contacts.reduce((max, c) => Math.max(max, c.id), 0) + 1;

const form = document.getElementById("contactForm");
const editIdInput = document.getElementById("editId");
const nameInput = document.getElementById("nameInput");
const emailInput = document.getElementById("emailInput");
const phoneInput = document.getElementById("phoneInput");
const notesInput = document.getElementById("notesInput");
const submitBtn = document.getElementById("submitBtn");
const cancelEditBtn = document.getElementById("cancelEditBtn");
const searchInput = document.getElementById("searchInput");
const contactList = document.getElementById("contactList");
const emptyMessage = document.getElementById("emptyMessage");

function render() {
  const search = searchInput.value.trim().toLowerCase();
  const filtered = contacts.filter(c => c.name.toLowerCase().includes(search));

  contactList.innerHTML = "";
  filtered
    .sort((a, b) => a.name.localeCompare(b.name))
    .forEach(c => {
      const li = document.createElement("li");
      li.className = "contact-card";
      li.innerHTML = `
        <div class="info">
          <strong>${escapeHtml(c.name)}</strong>
          <div class="meta">${escapeHtml(c.email)}${c.phone ? " · " + escapeHtml(c.phone) : ""}</div>
          ${c.notes ? `<div class="notes">${escapeHtml(c.notes)}</div>` : ""}
        </div>
        <div class="card-actions">
          <button class="edit" data-id="${c.id}">Modifier</button>
          <button class="del" data-id="${c.id}">Supprimer</button>
        </div>
      `;
      contactList.appendChild(li);
    });

  contactList.querySelectorAll(".edit").forEach(b => b.addEventListener("click", () => startEdit(Number(b.dataset.id))));
  contactList.querySelectorAll(".del").forEach(b => b.addEventListener("click", () => deleteContact(Number(b.dataset.id))));

  emptyMessage.classList.toggle("hidden", filtered.length > 0);
}

function escapeHtml(s) {
  const div = document.createElement("div");
  div.textContent = s;
  return div.innerHTML;
}

function startEdit(id) {
  const c = contacts.find(c => c.id === id);
  if (!c) return;
  editIdInput.value = c.id;
  nameInput.value = c.name;
  emailInput.value = c.email;
  phoneInput.value = c.phone || "";
  notesInput.value = c.notes || "";
  submitBtn.textContent = "Enregistrer";
  cancelEditBtn.classList.remove("hidden");
  nameInput.focus();
}

function resetForm() {
  form.reset();
  editIdInput.value = "";
  submitBtn.textContent = "Ajouter";
  cancelEditBtn.classList.add("hidden");
}

function deleteContact(id) {
  contacts = contacts.filter(c => c.id !== id);
  saveContacts(contacts);
  render();
}

form.addEventListener("submit", (e) => {
  e.preventDefault();
  const name = nameInput.value.trim();
  const email = emailInput.value.trim();
  const phone = phoneInput.value.trim();
  const notes = notesInput.value.trim();
  if (!name || !email) return;

  const editId = editIdInput.value ? Number(editIdInput.value) : null;
  if (editId) {
    const c = contacts.find(c => c.id === editId);
    if (c) {
      c.name = name; c.email = email; c.phone = phone; c.notes = notes;
    }
  } else {
    contacts.push({ id: nextId++, name, email, phone, notes });
  }
  saveContacts(contacts);
  resetForm();
  render();
});

cancelEditBtn.addEventListener("click", resetForm);
searchInput.addEventListener("input", render);

render();
