let expenses = [{ payer: "Alice", amount: 60, people: "Alice,Bob,Carol" }];

function render() {
  const div = document.getElementById("expenses");
  div.innerHTML = "<table><tr><th>Payeur</th><th>Montant</th><th>Partage entre</th></tr>" +
    expenses.map((e, i) => `<tr>
      <td><input data-i="${i}" data-f="payer" value="${e.payer}"></td>
      <td><input data-i="${i}" data-f="amount" type="number" value="${e.amount}"></td>
      <td><input data-i="${i}" data-f="people" value="${e.people}"></td>
    </tr>`).join("") + "</table>";

  div.querySelectorAll("input").forEach((input) => {
    input.addEventListener("input", () => {
      expenses[input.dataset.i][input.dataset.f] = input.dataset.f === "amount" ? +input.value : input.value;
      computeSettlements();
    });
  });
  computeSettlements();
}

function computeSettlements() {
  const balances = {};
  for (const e of expenses) {
    const people = e.people.split(",").map((p) => p.trim()).filter(Boolean);
    const share = e.amount / people.length;
    balances[e.payer] = (balances[e.payer] || 0) + e.amount;
    for (const p of people) balances[p] = (balances[p] || 0) - share;
  }

  const debtors = Object.entries(balances).filter(([, b]) => b < -0.01).map(([n, b]) => [n, -b]);
  const creditors = Object.entries(balances).filter(([, b]) => b > 0.01).map(([n, b]) => [n, b]);
  const settlements = [];

  let i = 0, j = 0;
  while (i < debtors.length && j < creditors.length) {
    const [debtor, debt] = debtors[i];
    const [creditor, credit] = creditors[j];
    const amount = Math.min(debt, credit);
    settlements.push(`${debtor} doit ${amount.toFixed(2)} a ${creditor}`);
    debtors[i][1] -= amount;
    creditors[j][1] -= amount;
    if (debtors[i][1] < 0.01) i++;
    if (creditors[j][1] < 0.01) j++;
  }

  document.getElementById("settlements").innerHTML = settlements.map((s) => `<p>${s}</p>`).join("") || "<p>Tout est equilibre.</p>";
}

document.getElementById("addExpense").addEventListener("click", () => {
  expenses.push({ payer: "", amount: 0, people: "" });
  render();
});

render();
