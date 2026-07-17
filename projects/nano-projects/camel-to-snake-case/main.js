function camelToSnake(str) {
  return str.replace(/([a-z0-9])([A-Z])/g, "$1_$2").toLowerCase();
}

const input = process.argv[2] || "helloWorldExample";
console.log(camelToSnake(input));
