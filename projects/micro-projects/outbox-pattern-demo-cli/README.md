# outbox-pattern-demo-cli

Demonstration du transactional outbox pattern : l'ecriture metier et l'evenement a publier
sont commits dans la MEME transaction SQL (une table `outbox`), puis un relay separe les publie.
Evite le probleme classique "j'ai commit la commande mais le message Kafka a echoue".

## Lancer
```bash
python outbox_demo.py
```
