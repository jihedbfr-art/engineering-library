let products = [
  { name: "Marque A", price: 3.5, quantity: 500 },
  { name: "Marque B", price: 6.0, quantity: 1000 },
];

function render() {
  const container = document.getElementById("products");
  container.innerHTML = products.map((p, i) => `
    <div class="product">
      <input data-i="${i}" data-f="name" value="${p.name}" style="width:100px">
      Prix: <input data-i="${i}" data-f="price" type="number" step="0.01" value="${p.price}">
      Quantite (g/ml): <input data-i="${i}" data-f="quantity" type="number" value="${p.quantity}">
    </div>
  `).join("");

  container.querySelectorAll("input").forEach((input) => {
    input.addEventListener("input", () => {
      const field = input.dataset.f;
      products[input.dataset.i][field] = field === "name" ? input.value : parseFloat(input.value) || 0;
      updateResults();
    });
  });
  updateResults();
}

function updateResults() {
  const withUnitPrice = products.map((p) => ({ ...p, unitPrice: p.price / p.quantity }));
  const cheapest = Math.min(...withUnitPrice.map((p) => p.unitPrice));

  document.querySelector("#result tbody").innerHTML = withUnitPrice.map((p) => `
    <tr class="${p.unitPrice === cheapest ? "best" : ""}">
      <td>${p.name}</td><td>${(p.unitPrice * 100).toFixed(3)} / 100 unites</td>
    </tr>
  `).join("");
}

document.getElementById("add").addEventListener("click", () => {
  products.push({ name: "Nouveau produit", price: 0, quantity: 1 });
  render();
});

render();
