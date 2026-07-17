const KEY = "flashcards-sm2";

function defaultDeck() {
  return [
    { front: "Que signifie IAM ?", back: "Identity and Access Management", interval: 1, dueDate: today() },
    { front: "Que signifie CQRS ?", back: "Command Query Responsibility Segregation", interval: 1, dueDate: today() },
    { front: "Que signifie OIDC ?", back: "OpenID Connect", interval: 1, dueDate: today() },
  ];
}

function today() {
  return new Date().toISOString().slice(0, 10);
}

function load() {
  return JSON.parse(localStorage.getItem(KEY) || JSON.stringify(defaultDeck()));
}

function save(deck) {
  localStorage.setItem(KEY, JSON.stringify(deck));
}

let deck = load();
let currentIndex = -1;
let showingBack = false;

function dueCards() {
  return deck.filter((c) => c.dueDate <= today());
}

function nextCard() {
  const due = dueCards();
  document.getElementById("due-count").textContent = `${due.length} carte(s) a reviser`;
  if (due.length === 0) {
    document.getElementById("card").textContent = "Rien a reviser aujourd'hui !";
    document.getElementById("controls").style.display = "none";
    return;
  }
  currentIndex = deck.indexOf(due[0]);
  showingBack = false;
  document.getElementById("card").textContent = deck[currentIndex].front;
  document.getElementById("controls").style.display = "none";
}

document.getElementById("card").addEventListener("click", () => {
  if (currentIndex < 0) return;
  showingBack = !showingBack;
  document.getElementById("card").textContent = showingBack ? deck[currentIndex].back : deck[currentIndex].front;
  document.getElementById("controls").style.display = showingBack ? "block" : "none";
});

document.querySelectorAll("#controls button").forEach((btn) => {
  btn.addEventListener("click", () => {
    const quality = parseInt(btn.dataset.quality, 10);
    const card = deck[currentIndex];
    card.interval = quality === 0 ? 1 : Math.round(card.interval * (quality === 3 ? 2 : 3));
    const due = new Date();
    due.setDate(due.getDate() + card.interval);
    card.dueDate = due.toISOString().slice(0, 10);
    save(deck);
    nextCard();
  });
});

nextCard();
