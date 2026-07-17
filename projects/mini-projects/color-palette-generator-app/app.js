function randomHex() {
  return "#" + Math.floor(Math.random() * 0xffffff).toString(16).padStart(6, "0");
}

function hslToHex(h, s, l) {
  s /= 100; l /= 100;
  const k = (n) => (n + h / 30) % 12;
  const a = s * Math.min(l, 1 - l);
  const f = (n) => l - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)));
  const toHex = (x) => Math.round(255 * x).toString(16).padStart(2, "0");
  return `#${toHex(f(0))}${toHex(f(8))}${toHex(f(4))}`;
}

function generatePalette() {
  const baseHue = Math.floor(Math.random() * 360);
  const colors = [0, 30, 60, 180, 210].map((offset) => hslToHex((baseHue + offset) % 360, 65, 55));

  const container = document.getElementById("palette");
  container.innerHTML = colors.map((c) => `<div class="swatch" style="background:${c}" data-color="${c}">${c}</div>`).join("");

  container.querySelectorAll(".swatch").forEach((el) => {
    el.addEventListener("click", () => {
      navigator.clipboard?.writeText(el.dataset.color);
      el.textContent = "Copie !";
      setTimeout(() => (el.textContent = el.dataset.color), 800);
    });
  });
}

document.getElementById("generate").addEventListener("click", generatePalette);
generatePalette();
