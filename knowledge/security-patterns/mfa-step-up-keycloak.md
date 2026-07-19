# MFA et step-up authentication (Keycloak)

## 🎯 Menace / objectif
Un mot de passe seul ne suffit plus à protéger un compte contre le phishing ou une fuite de
credentials réutilisés ailleurs. Le second facteur réduit ce risque ; le step-up va plus loin en ne
l'exigeant que pour les actions vraiment sensibles, sans dégrader l'expérience du reste de la
session.

## 🧠 Principe
Distinguer **authentification** (qui es-tu, une fois par session) de **step-up** (prouve-le à
nouveau, maintenant, pour cette action précise). Un utilisateur connecté depuis une heure via
mot de passe seul peut très bien consulter son tableau de bord sans friction — mais avant de
valider un virement, ou de changer les coordonnées bancaires du compte, on lui redemande un facteur
supplémentaire (OTP, WebAuthn). C'est le principe d'authentification adaptative : le niveau de
preuve demandé est proportionnel au risque de l'action, pas fixé une fois pour toutes à la
connexion.

## 🛠️ Mise en œuvre
Keycloak modélise ça via les **Authentication Flows** et le claim `acr`/`amr` du token.

```text
Realm Settings → Authentication → Flows
  - Browser flow : mot de passe seul suffisant pour la connexion standard
  - Flow dédié "step-up" : exige OTP en plus, déclenché à la demande via
    un paramètre de la requête d'autorisation (max_age / acr_values)
```

Côté client, la requête d'autorisation qui déclenche le step-up avant une action sensible :
```
GET /realms/{realm}/protocol/openid-connect/auth
  ?client_id=...
  &acr_values=step-up-otp
  &prompt=login
```

Côté backend, avant d'autoriser l'action sensible, vérifier que le token présenté porte bien le
niveau d'authentification attendu :
```java
Jwt jwt = ...;
String acr = jwt.getClaimAsString("acr");
if (!"step-up-otp".equals(acr)) {
    throw new InsufficientAuthenticationException("step-up required");
}
```

Un Authenticator SPI custom (voir
[keycloak-advanced.md](../backend/java-spring/keycloak-advanced.md)) permet d'aller plus loin :
déclencher le step-up automatiquement selon un score de risque (nouvelle IP, nouveau device,
montant au-dessus d'un seuil) plutôt que de compter sur le client pour le demander explicitement —
plus robuste, parce qu'un client compromis ou mal implémenté ne peut pas simplement sauter l'étape.

## ❌ Erreurs classiques
- Faire confiance au paramètre `acr_values` envoyé par le client sans vérifier côté backend que le
  token retourné porte effectivement le claim attendu — un client malveillant peut demander le flow
  faible et prétendre avoir fait le step-up.
- Un seul facteur MFA disponible (OTP par SMS uniquement) sans repli : un utilisateur qui perd son
  téléphone se retrouve bloqué hors de son compte, ce qui pousse vers des procédures de contournement
  bancales côté support — prévoir au moins un second moyen (codes de récupération, WebAuthn).
- Redemander le MFA à chaque requête plutôt qu'une fois par session/action — la friction excessive
  pousse les utilisateurs à chercher des raccourcis (mot de passe noté quelque part, etc.), ce qui
  dégrade la sécurité réelle au lieu de l'améliorer.

## ✅ Vérification
Se connecter avec mot de passe seul, confirmer l'accès aux fonctions non sensibles ; puis tenter
l'action sensible et vérifier que Keycloak redemande bien l'OTP avant de la laisser passer ; enfin,
forger ou rejouer un token sans le claim `acr` attendu contre l'endpoint sensible côté backend et
vérifier qu'il est bien rejeté indépendamment de ce que dit le frontend.

## 🔗 Liens
- [anti-bruteforce-keycloak.md](anti-bruteforce-keycloak.md)
- [oauth2-keycloak.md](oauth2-keycloak.md)
