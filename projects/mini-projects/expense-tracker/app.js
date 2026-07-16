const STORAGE_KEY = 'expense-tracker.expenses';
let expenses = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');

const form = document.getElementById('expense-form');
const list = document.getElementById('expense-list');
const totalEl = document.getElementById('total');
const chartEl = document.getElementById('chart');

document.getElementById('date').value = new Date().toISOString().slice(0, 10);

function save() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(expenses));
}

function render() {
  list.innerHTML = '';
  let total = 0;
  const byCategory = {};

  expenses.slice().sort((a, b) => b.date.localeCompare(a.date)).forEach(exp => {
    total += exp.amount;
    byCategory[exp.category] = (byCategory[exp.category] || 0) + exp.amount;

    const li = document.createElement('li');
    const info = document.createElement('span');
    info.innerHTML = `${escapeHtml(exp.label)} — <strong>${exp.amount.toFixed(2)} €</strong>
      <span class="exp-meta">(${exp.category}, ${exp.date})</span>`;

    const delBtn = document.createElement('button');
    delBtn.textContent = '✕';
    delBtn.addEventListener('click', () => {
      expenses = expenses.filter(e => e.id !== exp.id);
      save();
      render();
    });

    li.appendChild(info);
    li.appendChild(delBtn);
    list.appendChild(li);
  });

  totalEl.textContent = total.toFixed(2);
  renderChart(byCategory);
}

function renderChart(byCategory) {
  chartEl.innerHTML = '';
  const max = Math.max(1, ...Object.values(byCategory));
  Object.entries(byCategory).forEach(([category, amount]) => {
    const wrap = document.createElement('div');
    wrap.className = 'bar-wrap';
    const heightPct = Math.max(4, (amount / max) * 100);
    wrap.innerHTML = `
      <div class="bar-amount">${amount.toFixed(0)}€</div>
      <div class="bar" style="height:${heightPct}%"></div>
      <div class="bar-label">${category}</div>
    `;
    chartEl.appendChild(wrap);
  });
}

function escapeHtml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

form.addEventListener('submit', (e) => {
  e.preventDefault();
  const label = document.getElementById('label').value.trim();
  const amount = parseFloat(document.getElementById('amount').value);
  const category = document.getElementById('category').value;
  const date = document.getElementById('date').value;
  if (!label || !(amount > 0) || !date) return;

  expenses.push({ id: Date.now(), label, amount, category, date });
  form.reset();
  document.getElementById('date').value = new Date().toISOString().slice(0, 10);
  save();
  render();
});

render();
