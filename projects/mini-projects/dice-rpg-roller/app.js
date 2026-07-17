function rollDice(notation) {
  const match = notation.trim().match(/^(\d+)d(\d+)([+-]\d+)?$/i);
  if (!match) throw new Error("Notation invalide (ex: 2d6+3)");
  const [, countStr, sidesStr, bonusStr] = match;
  const count = parseInt(countStr, 10);
  const sides = parseInt(sidesStr, 10);
  const bonus = bonusStr ? parseInt(bonusStr, 10) : 0;

  const rolls = Array.from({ length: count }, () => 1 + Math.floor(Math.random() * sides));
  const total = rolls.reduce((a, b) => a + b, 0) + bonus;
  return { rolls, bonus, total };
}

document.getElementById("roll").addEventListener("click", () => {
  try {
    const { rolls, bonus, total } = rollDice(document.getElementById("notation").value);
    document.getElementById("total").textContent = `Total : ${total}`;
    const li = document.createElement("li");
    li.textContent = `[${rolls.join(", ")}] ${bonus ? (bonus > 0 ? "+" + bonus : bonus) : ""} = ${total}`;
    document.getElementById("history").prepend(li);
  } catch (e) {
    document.getElementById("total").textContent = e.message;
  }
});
