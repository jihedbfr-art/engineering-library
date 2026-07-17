# hotp-generator-cli

Genere un code HOTP (HMAC-based One-Time Password, RFC 4226) a partir d'un secret et d'un
compteur - la base historique sur laquelle TOTP a ete construit.

## Lancer

```bash
javac HotpGenerator.java && java HotpGenerator 12345678901234567890 0
```
