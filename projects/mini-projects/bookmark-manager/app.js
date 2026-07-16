const STORAGE_KEY = 'bookmark-manager.bookmarks';
let bookmarks = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
let searchTerm = '';
let tagFilter = '';

const form = document.getElementById('bookmark-form');
const list = document.getElementById('bookmark-list');
const searchInput = document.getElementById('search');
const tagFilterSelect = document.getElementById('tag-filter');

function save() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(bookmarks));
}

function allTags() {
  const tagSet = new Set();
  bookmarks.forEach(b => b.tags.forEach(t => tagSet.add(t)));
  return Array.from(tagSet).sort();
}

function renderTagFilter() {
  const current = tagFilterSelect.value;
  tagFilterSelect.innerHTML = '<option value="">Tous les tags</option>';
  allTags().forEach(tag => {
    const opt = document.createElement('option');
    opt.value = tag;
    opt.textContent = tag;
    tagFilterSelect.appendChild(opt);
  });
  tagFilterSelect.value = current;
}

function matches(bookmark) {
  const term = searchTerm.toLowerCase();
  const matchesSearch = !term ||
    bookmark.title.toLowerCase().includes(term) ||
    bookmark.url.toLowerCase().includes(term);
  const matchesTag = !tagFilter || bookmark.tags.includes(tagFilter);
  return matchesSearch && matchesTag;
}

function render() {
  list.innerHTML = '';
  const filtered = bookmarks.filter(matches);

  if (filtered.length === 0) {
    list.innerHTML = '<p style="color:#888;">Aucun favori.</p>';
  }

  filtered.forEach(b => {
    const li = document.createElement('li');
    const tagsHtml = b.tags.map(t => `<span class="tag">${escapeHtml(t)}</span>`).join('');
    li.innerHTML = `
      <button data-id="${b.id}">Supprimer</button>
      <a href="${escapeAttr(b.url)}" target="_blank">${escapeHtml(b.title)}</a>
      <div class="tags">${tagsHtml}</div>
    `;
    li.querySelector('button').addEventListener('click', () => {
      bookmarks = bookmarks.filter(x => x.id !== b.id);
      save();
      renderTagFilter();
      render();
    });
    list.appendChild(li);
  });
}

function escapeHtml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
function escapeAttr(s) {
  return escapeHtml(s).replace(/"/g, '&quot;');
}

form.addEventListener('submit', (e) => {
  e.preventDefault();
  const title = document.getElementById('title').value.trim();
  const url = document.getElementById('url').value.trim();
  const tags = document.getElementById('tags').value
    .split(',').map(t => t.trim()).filter(Boolean);
  if (!title || !url) return;

  bookmarks.push({ id: Date.now(), title, url, tags });
  form.reset();
  save();
  renderTagFilter();
  render();
});

searchInput.addEventListener('input', () => {
  searchTerm = searchInput.value.trim();
  render();
});

tagFilterSelect.addEventListener('change', () => {
  tagFilter = tagFilterSelect.value;
  render();
});

renderTagFilter();
render();
