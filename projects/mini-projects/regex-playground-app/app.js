function highlight() {
  const patternStr = document.getElementById("pattern").value;
  const flags = document.getElementById("flags").value;
  const text = document.getElementById("text").value;
  const output = document.getElementById("output");
  const countEl = document.getElementById("count");

  try {
    const regex = new RegExp(patternStr, flags);
    const matches = [...text.matchAll(new RegExp(patternStr, flags.includes("g") ? flags : flags + "g"))];
    countEl.textContent = matches.length;

    let result = "";
    let lastIndex = 0;
    for (const m of matches) {
      result += escapeHtml(text.slice(lastIndex, m.index)) + "<mark>" + escapeHtml(m[0]) + "</mark>";
      lastIndex = m.index + m[0].length;
    }
    result += escapeHtml(text.slice(lastIndex));
    output.innerHTML = result;
  } catch (e) {
    output.textContent = "Erreur : " + e.message;
    countEl.textContent = "0";
  }
}

function escapeHtml(s) {
  return s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

["pattern", "flags", "text"].forEach((id) =>
  document.getElementById(id).addEventListener("input", highlight)
);
highlight();
