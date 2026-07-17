function evaluate(password) {
  const checks = [
    { ok: password.length >= 12, label: "Au moins 12 caracteres" },
    { ok: /[a-z]/.test(password), label: "Contient une minuscule" },
    { ok: /[A-Z]/.test(password), label: "Contient une majuscule" },
    { ok: /\d/.test(password), label: "Contient un chiffre" },
    { ok: /[^A-Za-z0-9]/.test(password), label: "Contient un caractere special" },
  ];
  const score = checks.filter((c) => c.ok).length;
  return { score, checks };
}

document.getElementById("pw").addEventListener("input", (e) => {
  const { score, checks } = evaluate(e.target.value);
  const percent = (score / 5) * 100;
  const bar = document.getElementById("bar");
  bar.style.width = percent + "%";
  bar.style.background = percent < 40 ? "#e74c3c" : percent < 80 ? "#f39c12" : "#27ae60";

  const labels = ["Tres faible", "Faible", "Moyen", "Bon", "Fort", "Tres fort"];
  document.getElementById("label").textContent = labels[score];

  document.getElementById("feedback").innerHTML = checks
    .map((c) => `<li style="color:${c.ok ? "green" : "#999"}">${c.ok ? "✓" : "○"} ${c.label}</li>`)
    .join("");
});
