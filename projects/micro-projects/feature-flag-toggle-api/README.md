# feature-flag-toggle-api

Store de feature flags en memoire (ConcurrentHashMap), avec fallback false pour tout flag inconnu -
le minimum viable avant d'introduire un vrai systeme comme GrowthBook/LaunchDarkly.

## Lancer
```bash
javac FeatureFlags.java && java FeatureFlags
```
