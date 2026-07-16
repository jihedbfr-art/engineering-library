// 10 questions codées en dur
const QUESTIONS = [
  { q: "Quelle est la capitale de la France ?", options: ["Lyon", "Paris", "Marseille", "Nice"], correct: 1 },
  { q: "Combien de continents y a-t-il sur Terre ?", options: ["5", "6", "7", "8"], correct: 2 },
  { q: "Quel langage s'exécute nativement dans un navigateur ?", options: ["Python", "Java", "JavaScript", "C#"], correct: 2 },
  { q: "Qui a peint la Joconde ?", options: ["Van Gogh", "Léonard de Vinci", "Picasso", "Monet"], correct: 1 },
  { q: "Quelle planète est la plus proche du Soleil ?", options: ["Vénus", "Terre", "Mercure", "Mars"], correct: 2 },
  { q: "Combien font 7 x 8 ?", options: ["54", "56", "58", "64"], correct: 1 },
  { q: "Quel est le plus grand océan du monde ?", options: ["Atlantique", "Indien", "Arctique", "Pacifique"], correct: 3 },
  { q: "En quelle année a eu lieu la Révolution française ?", options: ["1789", "1804", "1848", "1914"], correct: 0 },
  { q: "Quel gaz les plantes absorbent-elles principalement ?", options: ["Oxygène", "Azote", "CO2", "Hydrogène"], correct: 2 },
  { q: "Quel est le symbole chimique de l'or ?", options: ["Ag", "Au", "Fe", "Or"], correct: 1 }
];

let current = 0;
let score = 0;
let answered = false;

const questionText = document.getElementById("questionText");
const optionsList = document.getElementById("options");
const progress = document.getElementById("progress");
const feedback = document.getElementById("feedback");
const nextBtn = document.getElementById("nextBtn");
const quizScreen = document.getElementById("quizScreen");
const resultScreen = document.getElementById("resultScreen");
const scoreText = document.getElementById("scoreText");
const restartBtn = document.getElementById("restartBtn");

function renderQuestion() {
  answered = false;
  feedback.textContent = "";
  nextBtn.classList.add("hidden");

  const item = QUESTIONS[current];
  progress.textContent = `Question ${current + 1} / ${QUESTIONS.length}`;
  questionText.textContent = item.q;
  optionsList.innerHTML = "";

  item.options.forEach((opt, idx) => {
    const li = document.createElement("li");
    li.textContent = opt;
    li.addEventListener("click", () => selectAnswer(idx));
    optionsList.appendChild(li);
  });
}

function selectAnswer(idx) {
  if (answered) return;
  answered = true;
  const item = QUESTIONS[current];
  const items = optionsList.querySelectorAll("li");
  items.forEach(li => li.classList.add("disabled"));

  if (idx === item.correct) {
    score++;
    items[idx].classList.add("correct");
    feedback.textContent = "Correct !";
    feedback.style.color = "#2a9d8f";
  } else {
    items[idx].classList.add("incorrect");
    items[item.correct].classList.add("correct");
    feedback.textContent = "Incorrect.";
    feedback.style.color = "#e63946";
  }
  nextBtn.classList.remove("hidden");
}

function nextQuestion() {
  current++;
  if (current >= QUESTIONS.length) {
    showResult();
  } else {
    renderQuestion();
  }
}

function showResult() {
  quizScreen.classList.add("hidden");
  resultScreen.classList.remove("hidden");
  scoreText.textContent = `Score : ${score} / ${QUESTIONS.length}`;
}

function restart() {
  current = 0;
  score = 0;
  quizScreen.classList.remove("hidden");
  resultScreen.classList.add("hidden");
  renderQuestion();
}

nextBtn.addEventListener("click", nextQuestion);
restartBtn.addEventListener("click", restart);

renderQuestion();
