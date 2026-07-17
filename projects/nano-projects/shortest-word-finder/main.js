function shortestWord(text) {
  return text.split(/\s+/).reduce((a, b) => (b.length < a.length ? b : a));
}

const text = process.argv.slice(2).join(" ") || "un elephant traverse la savane";
console.log(shortestWord(text));
