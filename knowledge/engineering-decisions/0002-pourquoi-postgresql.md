# 2. PostgreSQL par défaut pour les nouveaux services, Oracle seulement là où il l'était déjà

Statut : acceptée

## Contexte

La plateforme est un chantier de 14+ microservices Spring Boot construits à côté d'un système de
douane/commerce extérieur existant, qui tourne sous Oracle depuis des années et qui reste la
source de vérité pour tout ce qui touche aux échanges avec les administrations. Deux options se
présentaient dès le départ du découpage : garder Oracle partout parce que c'est ce que les DBA et
l'historique de l'équipe connaissent déjà bien, ou repartir sur un socle 100 % PostgreSQL pour
avoir un stack homogène.

Aucune des deux n'était satisfaisante telle quelle. Tout remettre sous Oracle pour des services
qui n'ont, au fond, qu'un besoin CRUD classique aurait fait payer une licence entreprise pour rien
sur treize services qui n'en tirent aucun bénéfice réel. À l'inverse, migrer le système existant
vers PostgreSQL uniquement par cohérence esthétique aurait été le genre de réécriture "on repart à
zéro" qui ne se justifie que par une contrainte forte — jamais par préférence, cf. le tableau de
[`postgresql-vs-oracle.md`](../database-engineering/postgresql-vs-oracle.md).

## Décision

Chaque nouveau service possède son propre schéma PostgreSQL 16, jamais partagé avec un autre
service. Le seul point de contact avec Oracle 21c est le service qui doit lire et écrire dans les
tables du système commerce extérieur existant ; cet accès passe par un adaptateur volontairement
fin (pas d'ORM riche dessus, juste les requêtes dont ce service a besoin), pour que la dépendance
Oracle reste confinée à ce seul point plutôt que de se diffuser dans le reste de la plateforme.

Rien n'empêche un futur service d'avoir, lui aussi, besoin de parler à Oracle — mais ce sera une
décision au cas par cas, pas une extension automatique du même adaptateur à d'autres domaines.

## Conséquences

Treize services sur quatorze n'ont plus de coût de licence Oracle à justifier, et récupèrent au
passage les extensions PostgreSQL modernes (GIN pour la recherche full-text, PGVector si un
service en a besoin un jour) sans équivalent natif direct côté Oracle. En échange, l'équipe garde
deux moteurs à opérer en parallèle au lieu d'un seul. Les DBA gardent une compétence Oracle active
pour ce seul service, et ça a un coût réel — pas seulement celui de la licence qu'on évite ailleurs.

Le vrai risque à surveiller, c'est que l'adaptateur Oracle grossisse discrètement au fil des
demandes ("juste une table de plus, ça ira plus vite") jusqu'à redevenir un point de couplage
central. Il mérite une revue régulière pour vérifier qu'il reste bien un adaptateur, et pas un
deuxième système en train de se reformer derrière son dos.
