function parseHeaders(text) {
  return text.split("\n").filter(Boolean).map((line) => {
    const [key, ...rest] = line.split(":");
    return [key.trim(), rest.join(":").trim()];
  });
}

function update() {
  const method = document.getElementById("method").value;
  const url = document.getElementById("url").value;
  const headers = parseHeaders(document.getElementById("headers").value);
  const body = document.getElementById("body").value.trim();

  let curl = `curl -X ${method} "${url}"`;
  headers.forEach(([k, v]) => (curl += ` \\n  -H "${k}: ${v}"`));
  if (body) curl += ` \\n  -d '${body}'`;
  document.getElementById("curl-output").textContent = curl;

  const headersObj = Object.fromEntries(headers);
  let fetchCode = `fetch("${url}", {\n  method: "${method}",\n  headers: ${JSON.stringify(headersObj, null, 2).replace(/\n/g, "\n  ")}`;
  if (body) fetchCode += `,\n  body: ${JSON.stringify(body)}`;
  fetchCode += "\n});";
  document.getElementById("fetch-output").textContent = fetchCode;
}

["method", "url", "headers", "body"].forEach((id) =>
  document.getElementById(id).addEventListener("input", update)
);
update();
