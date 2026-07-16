# PostgreSQL vs Oracle

> Comparatif basé sur une pratique réelle des deux moteurs (Oracle en environnement d'entreprise/
> télécom, PostgreSQL sur les projets microservices récents), pas une lecture de documentation.

## 🏗️ Architecture
- **Oracle** : processus serveur partagés entre sessions, lecture cohérente via segments d'undo.
- **PostgreSQL** : un processus OS par connexion, MVCC par versionnage de lignes — d'où
  l'importance du pool de connexions applicatif (cf.
  [performance-recipes/hikaricp-pool-sizing.md](../performance-recipes/hikaricp-pool-sizing.md)),
  bien plus déterminante que côté Oracle.

## 🔑 Index & optimiseur
Les deux disposent d'un optimiseur basé sur les coûts et d'index B-tree comme défaut. Oracle a une
longueur d'avance historique sur le partitionnement et les index bitmap pour les charges
décisionnelles ; PostgreSQL rattrape via des extensions (GIN, PGVector) qui n'ont pas
d'équivalent natif direct côté Oracle.

## 🔒 Licence & coût
Oracle : licence commerciale, coût significatif à l'échelle (cœurs, options avancées comme
Partitioning ou Data Guard vendues séparément). PostgreSQL : open-source, coût d'infrastructure
seulement — facteur décisif dans le choix pour de nouveaux projets sans contrainte legacy.

## ⚖️ Quand chacun s'impose
| Contexte | Choix qui s'impose |
|---|---|
| Système legacy déjà sous Oracle (ERP, BSS opérateur) | Rester sur Oracle — migrer une base de production critique n'est justifié que par un besoin fort, pas par préférence |
| Nouveau microservice sans contrainte historique | PostgreSQL — coût, écosystème open-source, extensions modernes (PGVector) |
| Besoin de fonctionnalités entreprise avancées (RAC, Data Guard mature) | Oracle reste en avance sur certains scénarios de haute disponibilité complexe |

## ⚠️ Piège du comparatif
Comparer les deux uniquement sur des benchmarks de performance bruts passe à côté de l'essentiel :
la vraie différence structurante est le **modèle de licence et le coût total de possession** à
l'échelle, pas la vitesse d'une requête isolée — les deux moteurs sont assez matures pour que la
performance pure soit rarement le facteur décisif en pratique.

## 🔗 Références
- [database-engineering/postgresql.md](postgresql.md)
- [database-engineering/oracle.md](oracle.md)
