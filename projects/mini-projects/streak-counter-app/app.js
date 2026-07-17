const STORAGE_KEY = "streak-checkins";

function loadCheckins() {
  return JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
}

function saveCheckins(checkins) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(checkins));
}

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

function computeStreak(checkins) {
  const dates = new Set(checkins);
  let streak = 0;
  let cursor = new Date();
  while (dates.has(cursor.toISOString().slice(0, 10))) {
    streak++;
    cursor.setDate(cursor.getDate() - 1);
  }
  return streak;
}

function render() {
  const checkins = loadCheckins();
  document.getElementById("streak").textContent = computeStreak(checkins);

  const calendar = document.getElementById("calendar");
  calendar.innerHTML = "";
  for (let i = 29; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    const dayStr = d.toISOString().slice(0, 10);
    const div = document.createElement("div");
    div.className = "day" + (checkins.includes(dayStr) ? " done" : "");
    div.title = dayStr;
    calendar.appendChild(div);
  }
}

document.getElementById("checkin").addEventListener("click", () => {
  const checkins = loadCheckins();
  if (!checkins.includes(todayStr())) {
    checkins.push(todayStr());
    saveCheckins(checkins);
  }
  render();
});

render();
