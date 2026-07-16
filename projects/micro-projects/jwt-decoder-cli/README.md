# jwt-decoder-cli

Décode le header et le payload d'un JWT (base64url + JSON) et les affiche joliment.

> **AVERTISSEMENT DE SÉCURITÉ** : ce script **NE VÉRIFIE PAS** la signature du token.
> Il se contente de décoder le contenu, comme jwt.io. Un token affiché ici n'est PAS validé —
> ne jamais faire confiance à son contenu sans vérifier la signature côté serveur avec la
> clé/secret appropriée.

## Lancer

```bash
python jwt_decoder.py "<token.jwt.ici>"
```

## Exemple

```bash
$ python jwt_decoder.py "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.xxx"
AVERTISSEMENT: ce decodeur ne verifie PAS la signature du JWT...

=== HEADER ===
{
  "alg": "HS256",
  "typ": "JWT"
}

=== PAYLOAD ===
{
  "sub": "1234567890"
}
```
