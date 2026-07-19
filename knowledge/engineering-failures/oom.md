# OOM / `OOMKilled` (limite mémoire du conteneur dépassée)

> Le pod redémarre brutalement, `kubectl describe pod` affiche `Last State: Terminated, Reason: OOMKilled` — le kernel Linux a tué le process parce qu'il a dépassé la limite mémoire du cgroup, pas parce que la JVM a levé une exception proprement.

## 🔍 Cause

À distinguer clairement d'un [memory leak](memory-leak.md) progressif : un OOMKilled peut être la conséquence finale d'un leak, mais tout aussi bien un dimensionnement incorrect entre le `-Xmx` de la JVM et la `limits.memory` du conteneur Kubernetes, sans aucun leak réel. Le piège classique : la JVM calcule son heap max par défaut en fonction de la mémoire visible du système hôte, pas de la limite cgroup du conteneur (comportement dépendant de la version de la JVM — les versions récentes sont "container-aware" mais peuvent encore mal évaluer si la limite du conteneur n'est pas explicitement communiquée). Résultat : la JVM pense avoir droit à plus de mémoire que ce que le conteneur autorise réellement, et le heap grossit légitimement (GC normal, pas de fuite) jusqu'à dépasser la limite cgroup — le kernel tue alors le process sans lui laisser la moindre chance de lever un `OutOfMemoryError` proprement gérable.

## 🚨 Symptômes

- Le pod redémarre sans warning applicatif dans les logs — aucune stack trace, aucun `OutOfMemoryError` dans les logs Java, parce que le process est tué de l'extérieur par le kernel avant que la JVM n'ait la moindre chance de logger quoi que ce soit.
- `kubectl describe pod` montre `OOMKilled` avec un code de sortie `137` (128 + signal SIGKILL 9).
- Redémarrages qui coïncident avec des pics de charge ou un batch particulier (traitement d'un gros fichier, requête avec un jeu de résultats volumineux chargé entièrement en mémoire) plutôt qu'avec une tendance progressive sur plusieurs heures — c'est le signal le plus fiable pour distinguer un dimensionnement incorrect d'un vrai leak.

## 🩺 Comment diagnostiquer

```bash
# 1. Confirmer le OOMKilled et la limite configurée
kubectl describe pod mon-pod | grep -A 5 "Last State"
kubectl get pod mon-pod -o jsonpath='{.spec.containers[0].resources}'

# 2. Vérifier ce que la JVM croit avoir comme heap max à l'intérieur du conteneur
kubectl exec mon-pod -- java -XX:+PrintFlagsFinal -version | grep -i maxheapsize

# 3. Si les logs applicatifs juste avant le kill montrent un traitement précis
# (import de fichier, requête avec pagination absente), corréler l'heure du
# OOMKilled avec les logs applicatifs pour confirmer un pic ponctuel plutôt
# qu'une tendance longue
kubectl logs mon-pod --previous | tail -100
```

## ✅ Solution

- **Aligner explicitement `-Xmx` et la limite du conteneur**, en laissant une marge pour la mémoire hors-heap de la JVM (metaspace, stacks de threads, buffers directs) — ne jamais fixer `-Xmx` égal à `limits.memory`, viser plutôt 70-75% de la limite conteneur pour le heap, le reste couvrant l'overhead JVM :
```yaml
resources:
  limits:
    memory: "1Gi"
env:
  - name: JAVA_OPTS
    value: "-XX:MaxRAMPercentage=70.0"   # laisse ~30% pour le hors-heap
```
- **Éviter de charger un jeu de données entier en mémoire** quand un traitement en flux (streaming) ou paginé est possible — le pic ponctuel qui déclenche l'OOM vient souvent d'un `findAll()` sans pagination ou d'un fichier entier chargé en `List` plutôt que traité ligne par ligne.
- **Vérifier que le dimensionnement `limits.memory` correspond à un besoin réel mesuré**, pas à une valeur copiée d'un autre service sans réflexion — un service qui traite occasionnellement de gros lots a besoin d'une marge que son trafic moyen ne laisse pas deviner.

## 🛡️ Prévention

- Toujours définir `-XX:MaxRAMPercentage` (JVM container-aware) plutôt qu'un `-Xmx` fixe en valeur absolue, pour que le heap s'adapte automatiquement si la limite du conteneur change sans qu'on ait à resynchroniser deux configurations séparées.
- Charger les gros volumes en flux (streaming JDBC, pagination, traitement par lot) par défaut pour tout endpoint ou job qui pourrait un jour traiter un volume imprévisible, pas seulement une fois le problème constaté en prod.
- Surveiller l'usage mémoire réel du conteneur (pas seulement le heap JVM signalé en interne) pour repérer un dimensionnement trop juste avant qu'il ne déclenche un vrai OOMKilled sous charge de pointe.

## 🔗 Liens
- [memory-leak.md](memory-leak.md) — cause différente (fuite progressive) mais même symptôme final si non détecté
- [debugging-recipes/kubernetes-crashloopbackoff.md](../debugging-recipes/kubernetes-crashloopbackoff.md) — un OOMKilled répété en boucle ressemble à un CrashLoopBackOff classique côté observation Kubernetes
