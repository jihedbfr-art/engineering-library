# `CrashLoopBackOff` sur un pod Kubernetes

> Le pod démarre, plante, redémarre, replante — Kubernetes espace de plus en plus les tentatives (backoff exponentiel) au lieu d'abandonner.

## Causes probables (fréquentes → rares)
1. L'application plante réellement au démarrage (exception non gérée, config manquante, dépendance externe injoignable au boot — base de données, autre service).
2. Le `readinessProbe`/`livenessProbe` est mal calibré : le conteneur démarre correctement mais met plus longtemps que `initialDelaySeconds` à devenir prêt, la probe échoue, Kubernetes tue le conteneur en pensant qu'il est bloqué.
3. Le process principal du conteneur se termine normalement (exit code 0) parce que l'image a été construite pour un usage ponctuel (script) et pas pour tourner en continu — Kubernetes le relance en boucle puisqu'un pod est censé rester up.
4. Limite de mémoire (`resources.limits.memory`) trop basse par rapport à ce que la JVM (ou tout autre runtime) consomme réellement au démarrage → OOMKilled, visible dans le `reason` du dernier état du conteneur.
5. Erreur de configuration au démarrage : `ConfigMap`/`Secret` référencé mais absent, variable d'environnement requise non injectée.

## Diagnostic pas-à-pas
```bash
# 1. Voir l'état du pod et le nombre de redémarrages
kubectl get pod <pod> -o wide

# 2. Regarder le "reason" du dernier crash — OOMKilled, Error, ou autre
kubectl describe pod <pod> | grep -A5 "Last State"

# 3. Logs du conteneur qui vient de crasher (pas le conteneur courant, qui vient
#    peut-être juste de redémarrer et n'a encore rien loggé)
kubectl logs <pod> --previous

# 4. Vérifier les probes configurées et leur timing
kubectl get pod <pod> -o yaml | grep -A8 "readinessProbe\|livenessProbe"

# 5. Si OOMKilled : comparer la limite mémoire au besoin réel
kubectl top pod <pod>
```

## Correctif
Selon la cause identifiée à l'étape précédente :
- **Crash applicatif réel** : corriger le bug ou la dépendance manquante — les logs `--previous` donnent presque toujours la stack trace exacte.
- **Probe mal calibrée** : augmenter `initialDelaySeconds` et/ou `periodSeconds` pour laisser le temps réel au démarrage (particulièrement fréquent avec une JVM Spring Boot qui a un temps de boot non négligeable — vérifier le temps de démarrage réel en local avant de fixer la probe).
- **Process qui se termine normalement** : ce n'est pas fait pour un Deployment classique — soit changer le workload en `Job`/`CronJob` si c'est bien un traitement ponctuel, soit garder le process principal actif si le conteneur doit tourner en continu.
- **OOMKilled** : augmenter `resources.limits.memory`, ou réduire l'empreinte mémoire du runtime (pour une JVM, vérifier `-Xmx` par rapport à la limite du conteneur — une JVM qui ne connaît pas la limite cgroup peut tenter d'allouer au-delà).
- **Config/secret manquant** : vérifier que le `ConfigMap`/`Secret` référencé existe bien dans le même namespace que le pod.

## Si ça ne suffit pas
Si les logs `--previous` sont vides même après plusieurs redémarrages, le conteneur crashe probablement avant même d'avoir initialisé son système de logging — dans ce cas, `kubectl describe pod` (section Events, pas Last State) donne souvent l'indice côté kubelet (image introuvable, volume qui ne monte pas, etc.) plutôt que côté application.
