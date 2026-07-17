const ONES = ["zero","un","deux","trois","quatre","cinq","six","sept","huit","neuf"];
const TEENS = ["dix","onze","douze","treize","quatorze","quinze","seize","dix-sept","dix-huit","dix-neuf"];
const TENS = ["","","vingt","trente","quarante","cinquante","soixante","soixante-dix","quatre-vingt","quatre-vingt-dix"];

function numberToWords(n) {
  if (n < 10) return ONES[n];
  if (n < 20) return TEENS[n - 10];
  if (n < 100) return TENS[Math.floor(n / 10)] + (n % 10 ? "-" + ONES[n % 10] : "");
  return String(n);
}

const n = parseInt(process.argv[2] || "42", 10);
console.log(numberToWords(n));
