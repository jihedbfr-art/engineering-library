const RULES = [
  { pattern: /bonjour|salut|hello/i, reply: "Bonjour ! Comment puis-je vous aider ?" },
  { pattern: /heure|quelle heure/i, reply: () => `Il est ${new Date().toLocaleTimeString()}.` },
  { pattern: /merci/i, reply: "Avec plaisir !" },
  { pattern: /nom|qui es.tu/i, reply: "Je suis un chatbot a regles, pas un LLM - je matche des patterns, rien de plus." },
  { pattern: /au revoir|bye/i, reply: "A bientot !" },
];

function respond(message) {
  for (const rule of RULES) {
    if (rule.pattern.test(message)) {
      return typeof rule.reply === "function" ? rule.reply() : rule.reply;
    }
  }
  return "Je n'ai pas de regle pour ca. Essayez 'bonjour', 'quelle heure', ou 'merci'.";
}

function addMessage(text, sender) {
  const p = document.createElement("p");
  p.className = sender;
  p.textContent = text;
  document.getElementById("chat").appendChild(p);
  document.getElementById("chat").scrollTop = 999999;
}

function send() {
  const input = document.getElementById("input");
  const message = input.value.trim();
  if (!message) return;
  addMessage(message, "user");
  addMessage(respond(message), "bot");
  input.value = "";
}

document.getElementById("send").addEventListener("click", send);
document.getElementById("input").addEventListener("keydown", (e) => { if (e.key === "Enter") send(); });
addMessage("Bonjour ! Je reponds a des patterns simples (regex), pas un vrai modele de langage.", "bot");
