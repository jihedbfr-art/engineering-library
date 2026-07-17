function parseIni(text) {
  const result = {};
  let currentSection = null;
  for (const rawLine of text.split("\n")) {
    const line = rawLine.trim();
    if (!line || line.startsWith(";") || line.startsWith("#")) continue;
    const sectionMatch = line.match(/^\[(.+)\]$/);
    if (sectionMatch) {
      currentSection = sectionMatch[1];
      result[currentSection] = {};
      continue;
    }
    const kv = line.match(/^([^=]+)=(.*)$/);
    if (kv && currentSection) {
      result[currentSection][kv[1].trim()] = kv[2].trim();
    }
  }
  return result;
}

function update() {
  const text = document.getElementById("input").value;
  document.getElementById("output").textContent = JSON.stringify(parseIni(text), null, 2);
}

document.getElementById("input").addEventListener("input", update);
update();
