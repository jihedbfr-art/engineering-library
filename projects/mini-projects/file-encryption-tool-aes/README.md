# file-encryption-tool-aes

Chiffrement/dechiffrement AES-256-CBC avec derivation de cle PBKDF2 (65536 iterations) depuis un
mot de passe - le pattern correct (pas de cle en dur, salt + IV aleatoires par execution).

## Lancer
```bash
javac FileEncryptor.java && java FileEncryptor encrypt "message secret" monMotDePasse
```
