function fibFast(n) {
  let [a, b] = [0n, 1n];
  for (let i = 0; i < n; i++) [a, b] = [b, a + b];
  return a;
}

const n = parseInt(process.argv[2] || "50", 10);
console.log(`F(${n}) = ${fibFast(n)}`);
