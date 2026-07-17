async function deriveKey(password, salt) {
  const keyMaterial = await crypto.subtle.importKey(
    "raw", new TextEncoder().encode(password), "PBKDF2", false, ["deriveKey"]
  );
  return crypto.subtle.deriveKey(
    { name: "PBKDF2", salt, iterations: 100000, hash: "SHA-256" },
    keyMaterial, { name: "AES-GCM", length: 256 }, false, ["encrypt", "decrypt"]
  );
}

async function encryptNote() {
  const text = document.getElementById("note").value;
  const password = document.getElementById("password").value;
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const key = await deriveKey(password, salt);

  const encrypted = await crypto.subtle.encrypt({ name: "AES-GCM", iv }, key, new TextEncoder().encode(text));
  const combined = new Uint8Array([...salt, ...iv, ...new Uint8Array(encrypted)]);
  document.getElementById("result").value = btoa(String.fromCharCode(...combined));
}

async function decryptNote() {
  try {
    const b64 = document.getElementById("result").value;
    const password = document.getElementById("password").value;
    const bytes = Uint8Array.from(atob(b64), (c) => c.charCodeAt(0));
    const salt = bytes.slice(0, 16);
    const iv = bytes.slice(16, 28);
    const data = bytes.slice(28);

    const key = await deriveKey(password, salt);
    const decrypted = await crypto.subtle.decrypt({ name: "AES-GCM", iv }, key, data);
    document.getElementById("note").value = new TextDecoder().decode(decrypted);
  } catch (e) {
    alert("Dechiffrement echoue (mauvais mot de passe ou donnee corrompue)");
  }
}

document.getElementById("encrypt").addEventListener("click", encryptNote);
document.getElementById("decrypt").addEventListener("click", decryptNote);
