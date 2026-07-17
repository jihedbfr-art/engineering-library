# jwt-generator-cli

Genere un JWT signe HS256 a partir du stdlib (hmac/hashlib), sans dependance externe.
Complement de `jwt-decoder-cli`.

## Lancer
```bash
python jwt_generator.py --sub alice --secret my-secret --exp-seconds 900
```
