# Claude Code — workflow d'ingénierie assisté

## 🎯 Objectif
Utiliser un agent de code (Claude Code) de façon productive sur un mono-repo comme celui-ci, sans
perdre le contrôle sur ce qui est committé ni introduire de dette invisible.

## 🧩 Contexte d'usage
Scaffolding de nouveaux projets (`nouveau-projet`), ajout d'une recette au cookbook, revue de code
avant merge, ou remplissage progressif de `knowledge/` — chaque fois qu'une tâche répétée peut être
packagée en skill plutôt que réexpliquée à chaque session.

## 🛠️ Recette
- **CLAUDE.md à la racine** : règles impératives du dépôt (stack de référence, ce qui est interdit,
  homogénéité des templates) — lu automatiquement en début de session, évite de répéter le
  contexte à chaque fois.
- **Skills projet** (`.claude/skills/`) : encapsulent un flux répété (scaffolder un projet, ajouter
  une entrée de savoir) avec son propre prompt détaillé — déclenchées par description en langage
  naturel, pas par commande mémorisée.
- **Revue avant commit** : toujours relire le diff produit (`git diff`, `git status`) avant de
  committer — un agent peut halluciner un détail plausible mais faux (nom de méthode, version de
  dépendance) ; vérifier contre le code réel, pas contre ce qui « semble » correct.
- **Un commit = une unité logique** : demander explicitement plusieurs petits commits plutôt qu'un
  mega-commit facilite la revue et le rollback ciblé.

## ✅ Résultat attendu
Un historique git lisible où chaque commit a une intention claire, un CLAUDE.md qui évite de
réexpliquer les règles du projet à chaque session, et des skills qui rendent les tâches répétées
reproductibles sans copier-coller de prompts.

## ⚠️ Piège
- **Confiance aveugle** : ne jamais committer un diff sans le relire — un agent peut être confiant
  et faux en même temps.
- **CLAUDE.md qui pourrit** : s'il n'est plus mis à jour quand les règles du projet changent, il
  devient une source de désinformation pire que l'absence de documentation.
- **Secrets dans le contexte** : ne jamais coller de vraies clés/API tokens dans un prompt ou un
  fichier lu par l'agent, même en local — traiter le contexte de l'agent comme potentiellement
  journalisé.
