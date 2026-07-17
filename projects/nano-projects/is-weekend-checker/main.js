function isWeekend(dateStr) {
  const day = new Date(dateStr).getDay();
  return day === 0 || day === 6;
}

const date = process.argv[2] || new Date().toISOString().slice(0, 10);
console.log(date, "->", isWeekend(date) ? "weekend" : "jour ouvre");
