# Test d'intégration S3

[English version](./testing-s3-backend.md)

`SocialPublisherEndToEndIT` démarre PostgreSQL et `adobe/s3mock:5.2.3` avec Testcontainers. L'application conserve son client Java MinIO ; S3Mock fournit le point d'accès S3 pendant ce test. Celui-ci publie un message texte avec le connecteur MOCK, vérifie le résultat enregistré et confirme l'existence du bucket de préparation des médias.

Lancer les tests du projet avec Docker disponible :

```bash
mvn -pl app -am verify
```

Ce contrôle couvre les opérations S3 utilisées par ce trajet de publication texte. Il ne prouve pas une compatibilité complète avec un serveur MinIO de production, l'envoi de médias ni la sécurité des URL présignées. L'image est figée à une version précise : l'ancien tag mutable `latest` dépendait d'une entrée de registre qui n'est plus publique.

Références : [Adobe S3Mock](https://github.com/adobe/S3Mock) et son [image Docker Hub](https://hub.docker.com/r/adobe/s3mock/tags).
