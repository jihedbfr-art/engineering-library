# graceful-shutdown-demo-cli

Capture SIGTERM/SIGINT pour finir les requetes en cours et fermer proprement avant de quitter,
au lieu de mourir brutalement au milieu d'une transaction - le comportement attendu d'un pod
Kubernetes qui recoit un SIGTERM avant d'etre kill -9.

## Lancer
```bash
python graceful_shutdown.py
```
