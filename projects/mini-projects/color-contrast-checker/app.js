function luminance(hex) {
  const rgb = [1, 3, 5].map((i) => parseInt(hex.substr(i, 2), 16) / 255);
  const [r, g, b] = rgb.map((c) => (c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4)));
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

function contrastRatio(hex1, hex2) {
  const l1 = luminance(hex1) + 0.05;
  const l2 = luminance(hex2) + 0.05;
  return l1 > l2 ? l1 / l2 : l2 / l1;
}

function update() {
  const fg = document.getElementById("fg").value;
  const bg = document.getElementById("bg").value;
  const preview = document.getElementById("preview");
  preview.style.color = fg;
  preview.style.background = bg;

  const ratio = contrastRatio(fg, bg);
  document.getElementById("ratio").textContent = ratio.toFixed(2) + ":1";

  let verdict;
  if (ratio >= 7) verdict = "AAA (excellent, meme petit texte)";
  else if (ratio >= 4.5) verdict = "AA (bon pour texte normal)";
  else if (ratio >= 3) verdict = "AA large text seulement";
  else verdict = "Insuffisant - a eviter";
  document.getElementById("verdict").textContent = verdict;
}

["fg", "bg"].forEach((id) => document.getElementById(id).addEventListener("input", update));
update();
