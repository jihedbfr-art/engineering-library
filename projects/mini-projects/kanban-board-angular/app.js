const STORAGE_KEY = 'kanban-board-angular.tasks';
let tasks = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');

const form = document.getElementById('add-form');
const input = document.getElementById('task-input');
const columns = document.querySelectorAll('.cards');

function save() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(tasks));
}

function render() {
  columns.forEach(col => {
    col.innerHTML = '';
    const status = col.dataset.status;
    tasks.filter(t => t.status === status).forEach(task => {
      const card = document.createElement('div');
      card.className = 'card';
      card.draggable = true;
      card.dataset.id = task.id;

      const span = document.createElement('span');
      span.textContent = task.text;

      const delBtn = document.createElement('button');
      delBtn.textContent = '✕';
      delBtn.addEventListener('click', () => {
        tasks = tasks.filter(t => t.id !== task.id);
        save();
        render();
      });

      card.appendChild(span);
      card.appendChild(delBtn);

      card.addEventListener('dragstart', () => {
        card.classList.add('dragging');
        card.dataset.dragging = 'true';
      });
      card.addEventListener('dragend', () => {
        card.classList.remove('dragging');
      });

      col.appendChild(card);
    });
  });
}

form.addEventListener('submit', (e) => {
  e.preventDefault();
  const text = input.value.trim();
  if (!text) return;
  tasks.push({ id: Date.now(), text, status: 'todo' });
  input.value = '';
  save();
  render();
});

columns.forEach(col => {
  col.addEventListener('dragover', (e) => {
    e.preventDefault();
    col.classList.add('drag-over');
  });
  col.addEventListener('dragleave', () => {
    col.classList.remove('drag-over');
  });
  col.addEventListener('drop', (e) => {
    e.preventDefault();
    col.classList.remove('drag-over');
    const draggingCard = document.querySelector('.card.dragging');
    if (!draggingCard) return;
    const id = Number(draggingCard.dataset.id);
    const task = tasks.find(t => t.id === id);
    if (task) {
      task.status = col.dataset.status;
      save();
      render();
    }
  });
});

render();
