const STORAGE_KEY = 'habit-tracker.habits';
let habits = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');

const form = document.getElementById('habit-form');
const list = document.getElementById('habit-list');

function save() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(habits));
}

function toDateStr(date) {
  return date.toISOString().slice(0, 10);
}

function last30Days() {
  const days = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  for (let i = 29; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    days.push(toDateStr(d));
  }
  return days;
}

function computeStreak(checkins) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  let streak = 0;
  let cursor = new Date(today);
  while (checkins.includes(toDateStr(cursor))) {
    streak++;
    cursor.setDate(cursor.getDate() - 1);
  }
  return streak;
}

function render() {
  list.innerHTML = '';
  const todayStr = toDateStr(new Date());
  const days = last30Days();

  habits.forEach(habit => {
    const card = document.createElement('div');
    card.className = 'habit-card';

    const streak = computeStreak(habit.checkins);

    const header = document.createElement('div');
    header.className = 'habit-header';
    header.innerHTML = `
      <h3>${escapeHtml(habit.name)}</h3>
      <span class="streak">🔥 ${streak} jour(s)</span>
    `;
    const delBtn = document.createElement('button');
    delBtn.className = 'delete';
    delBtn.textContent = '✕';
    delBtn.addEventListener('click', () => {
      habits = habits.filter(h => h.id !== habit.id);
      save();
      render();
    });
    header.appendChild(delBtn);

    const heatmap = document.createElement('div');
    heatmap.className = 'heatmap';
    days.forEach(day => {
      const cell = document.createElement('div');
      const done = habit.checkins.includes(day);
      cell.className = 'day-cell' + (done ? ' done' : '') + (day > todayStr ? ' future' : '');
      cell.title = day;
      if (day <= todayStr) {
        cell.addEventListener('click', () => {
          if (habit.checkins.includes(day)) {
            habit.checkins = habit.checkins.filter(d => d !== day);
          } else {
            habit.checkins.push(day);
          }
          save();
          render();
        });
      }
      heatmap.appendChild(cell);
    });

    card.appendChild(header);
    card.appendChild(heatmap);
    list.appendChild(card);
  });
}

function escapeHtml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

form.addEventListener('submit', (e) => {
  e.preventDefault();
  const name = document.getElementById('habit-name').value.trim();
  if (!name) return;
  habits.push({ id: Date.now(), name, checkins: [] });
  form.reset();
  save();
  render();
});

render();
