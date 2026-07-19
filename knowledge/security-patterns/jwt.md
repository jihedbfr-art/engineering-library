# JWT (JSON Web Token)

## 🎯 Menace / objectif
Transporter une identité et des claims d'autorisation entre parties, de façon vérifiable sans
appel réseau à chaque validation — la question de sécurité n'est pas "comment chiffrer un token"
mais "comment être sûr qu'il n'a pas été falsifié et qu'il correspond toujours à un accès valide".

## 🧠 Principe
Un JWT est trois parties encodées en Base64URL et séparées par des points :
`header.payload.signature`. Le header et le payload sont **lisibles par n'importe qui** sans clé
(Base64 n'est pas du chiffrement) — un JWT n'est jamais un endroit où mettre une donnée sensible.
Ce qui protège le token, c'est uniquement la **signature** : elle prouve que le payload n'a pas
été modifié depuis son émission par une partie qui détient la clé privée (ou le secret partagé).
Un backend qui reçoit un JWT vérifie la signature avec la clé publique de l'émetteur (JWKS
endpoint en pratique) — pas de session côté serveur à interroger, c'est ce qui rend le modèle
stateless et scalable horizontalement (voir [oauth2-keycloak.md](oauth2-keycloak.md) pour la mise
en œuvre côté Spring Resource Server).

```json
// Payload decode (lisible sans cle - jamais y mettre un secret)
{
  "sub": "user-42",
  "realm_access": { "roles": ["USER"] },
  "iss": "https://keycloak/realms/notesapp",
  "aud": "notes-api",
  "exp": 1721400000,
  "iat": 1721396400
}
```

## 🛠️ Mise en œuvre
Les claims qui doivent **toujours** être vérifiés côté serveur, indépendamment de la signature :
- `exp` (expiration) — un token expiré doit être rejeté même si la signature est valide.
- `iss` (issuer) — sinon un token émis par un autre realm/serveur d'autorisation passe.
- `aud` (audience) — sinon un token valide pour une autre API est accepté ici.

```java
// Spring valide exp/iss automatiquement via issuer-uri ; aud demande un validator explicite
@Bean
JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(issuerUri),
            new JwtClaimValidator<List<String>>("aud", aud -> aud.contains("notes-api"))
    ));
    return decoder;
}
```

## ❌ Erreurs classiques
- Décoder le payload côté client et lui faire confiance sans jamais vérifier la signature côté
  serveur — n'importe qui peut fabriquer un JWT avec le payload de son choix ; seule la
  vérification de signature distingue un vrai token d'un faux.
- Accepter l'algorithme `none` ou laisser le client dicter l'algorithme de signature via le header
  (`alg` confusion attack — faire valider par erreur un token signé HS256 avec la clé publique
  RS256 comme si c'était un secret partagé) — toujours forcer l'algorithme attendu côté validation,
  ne jamais le lire depuis le token lui-même.
- Mettre une donnée sensible dans le payload (numéro de carte, mot de passe, PII non nécessaire)
  en pensant que le token est "chiffré" — il ne l'est pas, seulement signé et encodé.
- Ne jamais vérifier `exp` côté serveur en assumant que le token a forcément été validé "quelque
  part en amont" (gateway) — chaque service qui reçoit un JWT directement doit le revalider
  entièrement, la défense en profondeur n'est pas optionnelle.

## ✅ Vérification
Forger un JWT avec une signature invalide (ou signé avec une mauvaise clé) et vérifier qu'il est
rejeté ; forger un token valide mais expiré et vérifier le rejet ; forger un token valide pour un
autre `aud`/`iss` et vérifier qu'il est rejeté même si la signature elle-même est correcte pour
son propre émetteur.

## 🔗 Liens
- [oauth2-keycloak.md](oauth2-keycloak.md) — la mise en œuvre complète côté Spring Resource Server
- [mfa-step-up-keycloak.md](mfa-step-up-keycloak.md) — le claim `acr` comme exemple de claim
  custom vérifié côté backend au-delà des claims standards
