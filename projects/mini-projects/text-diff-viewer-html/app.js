function lineDiff(before, after) {
  const a = before.split("\n");
  const b = after.split("\n");
  const maxLen = Math.max(a.length, b.length);
  const lines = [];
  for (let i = 0; i < maxLen; i++) {
    if (a[i] === b[i]) {
      lines.push({ type: "same", text: a[i] ?? "" });
    } else {
      if (a[i] !== undefined) lines.push({ type: "removed", text: a[i] });
      if (b[i] !== undefined) lines.push({ type: "added", text: b[i] });
    }
  }
  return lines;
}

document.getElementById("compute").addEventListener("click", () => {
  const before = document.getElementById("before").value;
  const after = document.getElementById("after").value;
  const lines = lineDiff(before, after);
  document.getElementById("diff").innerHTML = lines
    .map((l) => `<div class="${l.type}">${l.type === "added" ? "+ " : l.type === "removed" ? "- " : "  "}${l.text}</div>`)
    .join("");
});
