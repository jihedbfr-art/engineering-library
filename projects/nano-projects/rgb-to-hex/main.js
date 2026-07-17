function rgbToHex(r, g, b) {
  return "#" + [r, g, b].map((v) => v.toString(16).padStart(2, "0")).join("").toUpperCase();
}

const [r, g, b] = process.argv.slice(2, 5).map(Number);
console.log(rgbToHex(r || 255, g || 136, b || 0));
