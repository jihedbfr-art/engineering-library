# Image de test introuvable avant les assertions

> Un test d'intégration échoue pendant le démarrage de Testcontainers avec `404 pull access denied` pour `minio/minio:latest`. Aucun appel S3 du test n'a encore été exécuté.

English version: [test-container-image-unavailable.md](test-container-image-unavailable.md).

## Causes probables

1. Le dépôt, le tag ou le registre de l'image a changé ; `latest` ne garantit pas qu'une image existe toujours.
2. Le registre refuse l'accès (`401`) ou limite les téléchargements. Ce n'est pas le même diagnostic qu'un `404`.
3. Le runner ne joint pas le registre ; le journal montre alors un délai ou une erreur réseau.

## Diagnostic pas à pas

```bash
# Copier la référence exacte depuis le journal du job, puis tester sa disponibilité.
docker manifest inspect minio/minio:latest

# Pour un candidat examiné sur le site de son éditeur :
docker manifest inspect adobe/s3mock:5.2.3
docker pull adobe/s3mock:5.2.3
docker image inspect adobe/s3mock:5.2.3 --format '{{json .RepoDigests}}'
```

Relever la réponse exacte du registre. Vérifier le propriétaire du dépôt, le tag publié, l'architecture requise par la CI et le digest. Ne jamais copier des identifiants de registre dans un ticket ou un journal public.

## Correctif vérifié sur le cas étudié

Le test `SocialPublisherEndToEndIT` de `social-publisher-mcp` employait `minio/minio:latest`. La [correction proposée](https://github.com/jihedbfr-art/engineering-library/pull/45) utilise l'image versionnée `adobe/s3mock:5.2.3` du [projet Adobe S3Mock](https://github.com/adobe/S3Mock). Après une publication de texte, elle vérifie que le bucket `media-staging` existe. Le client MinIO Java de l'application reste en place. La suite Maven a réussi localement avec Docker et le build distant de cette proposition est passé. Voir les [limites du test dans la PR #45](https://github.com/jihedbfr-art/engineering-library/blob/18bfa7b/projects/top-projects/social-publisher-mcp/docs/testing-s3-backend.fr.md).

Ce test confirme le trajet exercé et la création du bucket. Il ne prouve ni l'envoi de médias ni la compatibilité complète avec MinIO en production. Avant de remplacer une autre image, choisir un substitut adapté aux opérations réellement utilisées et ajouter une assertion qui échouerait si ce service ne répondait pas correctement.

## Si ça ne suffit pas

Si le téléchargement réussit mais que le test échoue encore, examiner la readiness du conteneur, l'endpoint S3 et les assertions. Si le registre répond `401`, contrôler les droits d'accès sans exposer les secrets. Pour un problème général de conteneur qui démarre puis s'arrête, voir [docker-conteneur-exit.md](docker-conteneur-exit.md).
