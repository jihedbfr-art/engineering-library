# jwt-generator-cli

Signe un JWT HS256 minimal (header + payload + signature HMAC-SHA256) sans dependance -
complement de jwt-decoder-cli qui fait l'inverse.

## Lancer

```bash
python jwt_generator.py user-42 monSecret --expires-in 3600
```
