# url-encoder-decoder-cli

Encode/decode une chaine au format URL (application/x-www-form-urlencoded).

## Lancer
```bash
javac UrlCodec.java
java UrlCodec encode "hello world & co"
java UrlCodec decode "hello+world+%26+co"
```
