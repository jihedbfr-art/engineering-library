function bmiCategory(bmi) {
  if (bmi < 18.5) return "insuffisance ponderale";
  if (bmi < 25) return "corpulence normale";
  if (bmi < 30) return "surpoids";
  return "obesite";
}

const bmi = parseFloat(process.argv[2] || "22.5");
console.log(`IMC ${bmi} -> ${bmiCategory(bmi)}`);
