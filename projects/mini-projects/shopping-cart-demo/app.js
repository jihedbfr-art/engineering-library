// Catalogue codé en dur (pas de backend)
const PRODUCTS = [
  { id: 1, name: "Clavier mécanique", price: 79.90, emoji: "⌨️" },
  { id: 2, name: "Souris sans fil", price: 24.50, emoji: "🖱️" },
  { id: 3, name: "Casque audio", price: 59.00, emoji: "🎧" },
  { id: 4, name: "Webcam HD", price: 39.99, emoji: "📷" },
  { id: 5, name: "Ecran 27 pouces", price: 219.00, emoji: "🖥️" },
  { id: 6, name: "Lampe de bureau", price: 18.90, emoji: "💡" },
  { id: 7, name: "Chaise ergonomique", price: 149.00, emoji: "🪑" },
  { id: 8, name: "Disque SSD 1To", price: 89.90, emoji: "💾" },
  { id: 9, name: "Tapis de souris XL", price: 12.50, emoji: "🧩" },
  { id: 10, name: "Batterie externe", price: 29.90, emoji: "🔋" }
];

const CART_KEY = "shopping-cart-demo:cart";

function loadCart() {
  try {
    return JSON.parse(localStorage.getItem(CART_KEY)) || {};
  } catch {
    return {};
  }
}

function saveCart(cart) {
  localStorage.setItem(CART_KEY, JSON.stringify(cart));
}

let cart = loadCart(); // { productId: qty }

function renderCatalog() {
  const catalog = document.getElementById("catalog");
  catalog.innerHTML = "";
  PRODUCTS.forEach(p => {
    const card = document.createElement("div");
    card.className = "product-card";
    card.innerHTML = `
      <div class="emoji">${p.emoji}</div>
      <h3>${p.name}</h3>
      <div class="price">${p.price.toFixed(2)} €</div>
      <button data-id="${p.id}">Ajouter au panier</button>
    `;
    card.querySelector("button").addEventListener("click", () => addToCart(p.id));
    catalog.appendChild(card);
  });
}

function addToCart(id) {
  cart[id] = (cart[id] || 0) + 1;
  saveCart(cart);
  renderCart();
}

function removeFromCart(id) {
  if (!cart[id]) return;
  cart[id] -= 1;
  if (cart[id] <= 0) delete cart[id];
  saveCart(cart);
  renderCart();
}

function clearCart() {
  cart = {};
  saveCart(cart);
  renderCart();
}

function getCartTotal() {
  return Object.entries(cart).reduce((sum, [id, qty]) => {
    const product = PRODUCTS.find(p => p.id === Number(id));
    return product ? sum + product.price * qty : sum;
  }, 0);
}

function getCartCount() {
  return Object.values(cart).reduce((sum, qty) => sum + qty, 0);
}

function renderCart() {
  const total = getCartTotal();
  const count = getCartCount();
  document.getElementById("cartCount").textContent = count;
  document.getElementById("cartTotal").textContent = total.toFixed(2);
  document.getElementById("cartPanelTotal").textContent = total.toFixed(2);

  const list = document.getElementById("cartItems");
  list.innerHTML = "";
  Object.entries(cart).forEach(([id, qty]) => {
    const product = PRODUCTS.find(p => p.id === Number(id));
    if (!product) return;
    const li = document.createElement("li");
    li.innerHTML = `
      <span class="item-name">${product.emoji} ${product.name} x${qty}</span>
      <span>${(product.price * qty).toFixed(2)} €</span>
      <button data-id="${id}" title="Retirer un exemplaire">-</button>
    `;
    li.querySelector("button").addEventListener("click", () => removeFromCart(Number(id)));
    list.appendChild(li);
  });
}

document.getElementById("toggleCartBtn").addEventListener("click", () => {
  document.getElementById("cartPanel").classList.toggle("hidden");
});
document.getElementById("clearCartBtn").addEventListener("click", clearCart);

renderCatalog();
renderCart();
