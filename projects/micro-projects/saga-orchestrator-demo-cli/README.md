# saga-orchestrator-demo-cli

Pattern Saga (orchestration) : une sequence d'etapes, chacune avec sa propre compensation.
Si une etape echoue, les etapes deja executees sont annulees en sens inverse - la maniere de
gerer une transaction distribuee sans 2PC entre microservices.

## Lancer
```bash
python saga_demo.py
```
