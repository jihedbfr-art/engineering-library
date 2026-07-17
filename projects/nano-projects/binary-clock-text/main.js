function toBinary4(n) {
  return n.toString(2).padStart(4, "0");
}

function binaryClock(date = new Date()) {
  const h = date.getHours(), m = date.getMinutes(), s = date.getSeconds();
  return [h, m, s].map((v) => `${toBinary4(Math.floor(v / 10))} ${toBinary4(v % 10)}`).join("  ");
}

console.log(binaryClock());
