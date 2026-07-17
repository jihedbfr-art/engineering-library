# mini-key-value-store-server

Store cle-valeur HTTP minimal (com.sun.net.httpserver, zero dependance) : GET/PUT/DELETE sur
`/kv/<cle>`, en memoire (ConcurrentHashMap). Le squelette d'un Redis jouet.

## Lancer
```bash
javac KeyValueServer.java && java KeyValueServer

curl -X PUT -d "hello" http://localhost:8090/kv/foo
curl http://localhost:8090/kv/foo
curl -X DELETE http://localhost:8090/kv/foo
```
