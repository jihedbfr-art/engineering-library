function decimalToBinary(n) {
  return n.toString(2);
}

const n = parseInt(process.argv[2] || "42", 10);
console.log(`${n} = ${decimalToBinary(n)} en binaire`);
