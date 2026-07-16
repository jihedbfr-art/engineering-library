const STORAGE_KEY = "mood-tracker-app:entries";

const MOODS = [
  { value: 1, emoji: "😢" },
  { value: 2, emoji: "😕" },
  { value: 3, emoji: "😐" },
  { value: 4, emoji: "🙂" },
  { value: 5, emoji: "😄" }
];

const DAY_LABELS = ["Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"];

function loadEntries() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {};
  } catch {
    return {};
  }
}

function saveEntries(entries) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(entries));
}

let entries = loadEntries(); // { "YYYY-MM-DD": { mood: 1-5, note: "" } }
let selectedMood = null;

const todayLabel = document.getElementById("todayLabel");
const moodPicker = document.getElementById("moodPicker");
const noteInput = document.getElementById("noteInput");
const saveBtn = document.getElementById("saveBtn");
const saveStatus = document.getElementById("saveStatus");
const monthLabel = document.getElementById("monthLabel");
const calendarGrid = document.getElementById("calendarGrid");
const prevMonthBtn = document.getElementById("prevMonthBtn");
const nextMonthBtn = document.getElementById("nextMonthBtn");

function todayKey() {
  const d = new Date();
  return formatDateKey(d);
}

function formatDateKey(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

let viewYear, viewMonth; // viewMonth: 0-11

function initToday() {
  const now = new Date();
  viewYear = now.getFullYear();
  viewMonth = now.getMonth();

  todayLabel.textContent = "Aujourd'hui — " + now.toLocaleDateString("fr-FR", {
    weekday: "long", year: "numeric", month: "long", day: "numeric"
  });

  const existing = entries[todayKey()];
  selectedMood = existing ? existing.mood : null;
  noteInput.value = existing ? existing.note || "" : "";

  renderMoodPicker();
}

function renderMoodPicker() {
  moodPicker.innerHTML = "";
  MOODS.forEach(m => {
    const btn = document.createElement("button");
    btn.textContent = m.emoji;
    btn.className = m.value === selectedMood ? "selected" : "";
    btn.addEventListener("click", () => {
      selectedMood = m.value;
      renderMoodPicker();
    });
    moodPicker.appendChild(btn);
  });
}

function saveToday() {
  if (!selectedMood) {
    saveStatus.textContent = "Choisissez une humeur d'abord.";
    return;
  }
  entries[todayKey()] = { mood: selectedMood, note: noteInput.value.trim() };
  saveEntries(entries);
  saveStatus.textContent = "Enregistré !";
  renderCalendar();
  setTimeout(() => { saveStatus.textContent = ""; }, 2000);
}

function renderCalendar() {
  const monthNames = ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"];
  monthLabel.textContent = `${monthNames[viewMonth]} ${viewYear}`;

  calendarGrid.innerHTML = "";
  DAY_LABELS.forEach(label => {
    const head = document.createElement("div");
    head.className = "calendar-cell empty";
    head.textContent = label;
    head.style.fontWeight = "bold";
    head.style.color = "#555";
    calendarGrid.appendChild(head);
  });

  const firstDay = new Date(viewYear, viewMonth, 1);
  // Lundi = 0 ... Dimanche = 6
  const leadingBlanks = (firstDay.getDay() + 6) % 7;
  const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();

  for (let i = 0; i < leadingBlanks; i++) {
    const blank = document.createElement("div");
    blank.className = "calendar-cell empty";
    calendarGrid.appendChild(blank);
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(viewYear, viewMonth, day);
    const key = formatDateKey(date);
    const entry = entries[key];
    const cell = document.createElement("div");
    cell.className = "calendar-cell";
    if (key === todayKey()) cell.classList.add("today");
    const moodEmoji = entry ? MOODS.find(m => m.value === entry.mood)?.emoji || "" : "";
    cell.innerHTML = `<span class="day-num">${day}</span><span class="mood-emoji">${moodEmoji}</span>`;
    if (entry && entry.note) cell.title = entry.note;
    calendarGrid.appendChild(cell);
  }
}

prevMonthBtn.addEventListener("click", () => {
  viewMonth--;
  if (viewMonth < 0) { viewMonth = 11; viewYear--; }
  renderCalendar();
});

nextMonthBtn.addEventListener("click", () => {
  viewMonth++;
  if (viewMonth > 11) { viewMonth = 0; viewYear++; }
  renderCalendar();
});

saveBtn.addEventListener("click", saveToday);

initToday();
renderCalendar();
