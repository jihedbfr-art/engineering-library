let questions = [];

function render() {
  const container = document.getElementById("questions");
  container.innerHTML = questions.map((q, i) => `
    <fieldset>
      <legend>Question ${i + 1}</legend>
      <input placeholder="Intitule" data-i="${i}" data-f="text" value="${q.text}"><br><br>
      <input placeholder="Options (separees par des virgules)" data-i="${i}" data-f="options" value="${q.options}">
    </fieldset>
  `).join("");

  container.querySelectorAll("input").forEach((input) => {
    input.addEventListener("input", () => {
      questions[input.dataset.i][input.dataset.f] = input.value;
    });
  });
}

document.getElementById("addQuestion").addEventListener("click", () => {
  questions.push({ text: "", options: "" });
  render();
});

document.getElementById("exportJson").addEventListener("click", () => {
  const structured = questions.map((q) => ({
    question: q.text,
    options: q.options.split(",").map((o) => o.trim()).filter(Boolean),
  }));
  document.getElementById("output").textContent = JSON.stringify(structured, null, 2);
});

questions.push({ text: "Quel est votre langage prefere ?", options: "Java, Python, JavaScript" });
render();
