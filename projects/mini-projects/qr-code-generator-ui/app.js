function simpleHash(str, seed) {
  let hash = seed;
  for (let i = 0; i < str.length; i++) {
    hash = (hash * 31 + str.charCodeAt(i)) >>> 0;
  }
  return hash;
}

function generateGrid(text, size = 21) {
  const grid = document.getElementById("grid");
  grid.style.gridTemplateColumns = `repeat(${size}, 8px)`;
  grid.innerHTML = "";
  for (let i = 0; i < size * size; i++) {
    const cell = document.createElement("div");
    const bit = simpleHash(text + i, i * 7 + 13) % 2;
    cell.style.background = bit ? "#000" : "#fff";
    grid.appendChild(cell);
  }
}

document.getElementById("generate").addEventListener("click", () => {
  generateGrid(document.getElementById("text").value);
});

generateGrid(document.getElementById("text").value);
