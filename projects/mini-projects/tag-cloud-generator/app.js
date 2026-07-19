const STOPWORDS = new Set([
  "le", "la", "les", "un", "une", "des", "de", "du", "et", "a", "au", "aux",
  "en", "sur", "sous", "dans", "que", "qui", "ce", "cette", "ces", "il", "elle",
  "ils", "elles", "je", "tu", "nous", "vous", "est", "sont", "pour", "avec", "par"
]);

function wordFrequencies(text, ignoreStopwords) {
  const words = text.toLowerCase().match(/[a-zà-ÿ]+/g) || [];
  const counts = {};
  for (const word of words) {
    if (word.length < 2) continue;
    if (ignoreStopwords && STOPWORDS.has(word)) continue;
    counts[word] = (counts[word] || 0) + 1;
  }
  return counts;
}

function renderCloud(counts) {
  const cloud = document.getElementById("cloud");
  cloud.innerHTML = "";

  const entries = Object.entries(counts).sort((a, b) => b[1] - a[1]).slice(0, 40);
  if (entries.length === 0) {
    cloud.textContent = "Rien a afficher.";
    return;
  }

  const maxCount = entries[0][1];
  const minCount = entries[entries.length - 1][1];
  const minSize = 14, maxSize = 48;

  // shuffle pour ne pas afficher strictement du plus grand au plus petit,
  // plus proche visuellement d'un vrai "nuage"
  const shuffled = entries.slice().sort(() => Math.random() - 0.5);

  for (const [word, count] of shuffled) {
    const ratio = maxCount === minCount ? 1 : (count - minCount) / (maxCount - minCount);
    const size = minSize + ratio * (maxSize - minSize);
    const span = document.createElement("span");
    span.textContent = word;
    span.style.fontSize = `${size}px`;
    span.title = `${count} occurrence(s)`;
    cloud.appendChild(span);
  }
}

document.getElementById("generate").addEventListener("click", () => {
  const text = document.getElementById("text").value;
  const ignoreStopwords = document.getElementById("ignoreStopwords").checked;
  renderCloud(wordFrequencies(text, ignoreStopwords));
});

document.getElementById("generate").click();
