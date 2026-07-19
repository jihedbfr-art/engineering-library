# Memory leak (JVM, heap qui ne redescend jamais)

> L'usage mémoire de l'application grimpe en continu sur plusieurs heures ou jours, le GC tourne de plus en plus souvent sans jamais reconquérir l'espace attendu, jusqu'au crash ou au redémarrage forcé par l'orchestrateur.

## 🔍 Cause

Un memory leak Java n'est presque jamais "de la mémoire perdue" au sens C/C++ — le garbage collector fait toujours son travail correctement. Le vrai problème : des objets restent **accessibles** depuis une racine GC (une référence encore vivante quelque part) alors qu'ils ne devraient logiquement plus servir à rien. Les coupables classiques : une collection statique ou un cache maison qui grossit sans jamais évincer d'entrées (`static Map<String, Object> cache = new HashMap<>()` qu'on remplit mais qu'on ne vide jamais), un listener ou callback enregistré mais jamais désenregistré (chaque requête ajoute un listener sur un objet à durée de vie longue, personne ne le retire), ou un `ThreadLocal` non nettoyé dans un contexte de pool de threads — le thread est réutilisé indéfiniment par le pool, donc la valeur `ThreadLocal` de la requête précédente reste accrochée bien après que cette requête soit terminée.

## 🚨 Symptômes

- Courbe mémoire en dents de scie qui monte globalement dans le temps : chaque cycle de GC redescend un peu, mais jamais jusqu'au niveau du cycle précédent — le plancher monte, pas juste le plafond.
- Fréquence des GC qui augmente progressivement sur plusieurs heures/jours (le GC travaille plus dur pour libérer de moins en moins d'espace réellement récupérable).
- Finit par un `OutOfMemoryError: Java heap space`, ou par un redémarrage du pod côté Kubernetes si la mémoire du process JVM dépasse la limite du conteneur (voir [oom.md](oom.md) pour la distinction avec un OOMKilled ponctuel non lié à un leak).

## 🩺 Comment diagnostiquer

```bash
# 1. Confirmer la tendance sur la durée avant de chercher la cause — un seul
# heap dump ponctuel ne dit rien sur la tendance, il faut comparer dans le temps
jstat -gcutil <pid> 5000   # échantillon toutes les 5s, observer OLD (vieille génération)

# 2. Prendre deux heap dumps espacés dans le temps (ex: à 1h d'intervalle sous charge)
jmap -dump:live,format=b,file=heap1.hprof <pid>
# ... attendre, laisser l'application tourner sous charge réelle ...
jmap -dump:live,format=b,file=heap2.hprof <pid>

# 3. Comparer les deux dumps dans Eclipse Memory Analyzer (MAT) — le rapport
# "Leak Suspects" pointe directement les classes dont le nombre d'instances
# a explosé entre les deux dumps, avec le chemin de références (GC roots) qui
# les garde vivantes
```
Le chemin de références affiché par MAT est l'information la plus utile : il montre exactement quelle référence statique/listener/ThreadLocal garde l'objet accroché, ce qui transforme un "quelque chose fuit quelque part" en un point précis du code à corriger.

## ✅ Solution

Selon la cause identifiée dans le chemin de références :
- **Cache non borné** → remplacer par un cache avec politique d'éviction explicite (taille max, TTL) — Caffeine plutôt qu'une `HashMap` statique maison.
- **Listener non désenregistré** → s'assurer que chaque `register()` a un `unregister()` symétrique appelé au bon moment du cycle de vie (fin de requête, destruction du bean).
- **ThreadLocal non nettoyé** → toujours l'envelopper dans un `try/finally` qui appelle `remove()`, particulièrement critique dans un contexte de pool de threads réutilisés :
```java
private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

void handle(Request request) {
    CONTEXT.set(new RequestContext(request));
    try {
        // ... traitement ...
    } finally {
        CONTEXT.remove(); // sans ce remove(), le thread réutilisé par le pool garde la référence
    }
}
```

## 🛡️ Prévention

- Toute collection statique ou tout cache maison doit avoir une politique d'éviction explicite dès sa création, jamais "on ajoutera l'éviction plus tard si ça pose problème" — c'est précisément le genre de dette qui ne se révèle qu'en production sous charge réelle sur la durée.
- Charge de test longue durée (plusieurs heures, pas juste un test de charge de quelques minutes) avant mise en production d'un changement qui introduit un nouveau cache ou listener — un leak lent n'apparaît jamais sur un test de charge court.
- Surveiller la tendance de la vieille génération (old gen) en production dans le temps, pas juste l'usage mémoire instantané — une alerte sur "mémoire haute" ponctuelle ne distingue pas un pic de charge normal d'un leak progressif, une alerte sur la tendance sur plusieurs heures le fait.

## 🔗 Liens
- [oom.md](oom.md) — le mode de défaillance final si le leak n'est pas détecté à temps, mais aussi une cause bien plus large (limite mal dimensionnée, pas forcément un leak)
- [jvm-gc-pauses.md](jvm-gc-pauses.md) — un GC qui travaille de plus en plus dur à cause d'un leak finit aussi par dégrader la latence, pas seulement par crasher
