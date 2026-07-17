function isBalanced(str) {
  const pairs = { ")": "(", "]": "[", "}": "{" };
  const stack = [];
  for (const c of str) {
    if ("([{".includes(c)) stack.push(c);
    else if (")]}".includes(c) && stack.pop() !== pairs[c]) return false;
  }
  return stack.length === 0;
}

const input = process.argv[2] || "([{}])";
console.log(input, "->", isBalanced(input) ? "equilibre" : "desequilibre");
