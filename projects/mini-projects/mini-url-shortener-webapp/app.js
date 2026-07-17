const KEY = "url-shortener-map";
const ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

function load() {
  return JSON.parse(localStorage.getItem(KEY) || "{}");
}

function save(map) {
  localStorage.setItem(KEY, JSON.stringify(map));
}

function toBase62(num) {
  if (num === 0) return ALPHABET[0];
  let result = "";
  while (num > 0) {
    result = ALPHABET[num % 62] + result;
    num = Math.floor(num / 62);
  }
  return result;
}

function render() {
  const map = load();
  document.getElementById("list").innerHTML = Object.entries(map).map(([code, url]) =>
    `<div class="entry"><b>/${code}</b> → ${url}</div>`
  ).reverse().join("");
}

document.getElementById("shorten").addEventListener("click", () => {
  const url = document.getElementById("url").value.trim();
  if (!url) return;
  const map = load();
  const id = Object.keys(map).length + 1000;
  const code = toBase62(id);
  map[code] = url;
  save(map);
  document.getElementById("url").value = "";
  render();
});

render();
