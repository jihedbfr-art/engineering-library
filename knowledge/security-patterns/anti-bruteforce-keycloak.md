# Anti-bruteforce sur l'authentification (Keycloak)

## 🎯 Menace / objectif
Empêcher un attaquant de tester massivement des combinaisons identifiant/mot de passe (attaque par
force brute ou credential stuffing) contre l'endpoint de connexion.

## 🧠 Principe
Détecter et pénaliser les échecs de connexion répétés sur un même compte ou une même IP en
augmentant progressivement le délai avant nouvelle tentative, jusqu'au verrouillage temporaire du
compte au-delà d'un seuil.

## 🛠️ Mise en œuvre
Keycloak expose une protection anti-bruteforce native au niveau du realm, avec des délais
progressifs configurables :
```text
Realm Settings → Security Defenses → Brute Force Detection
  - Max Login Failures: 5
  - Wait Increment: 60s (délai qui augmente à chaque échec supplémentaire)
  - Max Wait: 15min
  - Quick Login Check in ms: 1000 (détecte les tentatives trop rapprochées, typiques d'un script)
```
Pour un besoin plus fin que la protection native (ex. logique métier spécifique, notification à
l'équipe sécurité au-delà d'un seuil), un authenticator SPI custom (cf. mise en œuvre réelle sur le
projet TTN) permet d'étendre ce comportement.

## ❌ Erreurs classiques
- Verrouillage uniquement basé sur l'IP → un attaquant distribué (botnet) contourne facilement ;
  combiner IP et compte cible.
- Message d'erreur qui révèle si le compte existe ("mot de passe incorrect" vs "compte
  inexistant") → facilite l'énumération de comptes valides, préférer un message générique unique.

## ✅ Vérification
Simuler plusieurs échecs de connexion consécutifs sur un compte de test et vérifier que le délai
d'attente augmente puis que le compte se verrouille temporairement au seuil configuré, avec un
message d'erreur qui ne distingue pas "compte inexistant" de "mot de passe incorrect".
