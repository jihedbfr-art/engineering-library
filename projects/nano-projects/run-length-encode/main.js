function runLengthEncode(str) {
  let result = "";
  let i = 0;
  while (i < str.length) {
    let count = 1;
    while (str[i] === str[i + count]) count++;
    result += count + str[i];
    i += count;
  }
  return result;
}

const input = process.argv[2] || "aaabbbccd";
console.log(runLengthEncode(input));
