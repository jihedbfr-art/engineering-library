let allLines = [];

document.getElementById("fileInput").addEventListener("change", (e) => {
  const file = e.target.files[0];
  const reader = new FileReader();
  reader.onload = () => {
    allLines = reader.result.split("\n").filter(Boolean);
    render();
  };
  reader.readAsText(file);
});

function render() {
  const level = document.getElementById("levelFilter").value;
  const search = document.getElementById("search").value.toLowerCase();
  const container = document.getElementById("logs");
  container.innerHTML = "";

  allLines
    .filter((line) => !level || line.includes(level))
    .filter((line) => !search || line.toLowerCase().includes(search))
    .forEach((line) => {
      const div = document.createElement("div");
      div.className = "line " + (["ERROR", "WARN", "INFO"].find((l) => line.includes(l)) || "");
      div.textContent = line;
      container.appendChild(div);
    });
}

document.getElementById("levelFilter").addEventListener("change", render);
document.getElementById("search").addEventListener("input", render);
