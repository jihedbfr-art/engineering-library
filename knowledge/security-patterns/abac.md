# ABAC (Attribute-Based Access Control)

## 🎯 Menace / objectif
Autoriser une action selon des attributs contextuels (qui est l'utilisateur, quelle est la ressource, dans quelles conditions) plutôt que selon un rôle fixe — nécessaire dès qu'une règle d'accès ne se réduit pas à "cette catégorie d'utilisateurs peut faire cette action", mais dépend de la relation entre l'utilisateur et la ressource précise.

## 🧠 Principe
RBAC répond à "quel rôle a cet utilisateur ?". ABAC répond à une question plus riche : "cet utilisateur, avec ces attributs (département, niveau, ancienneté), peut-il agir sur cette ressource, avec ses propres attributs (propriétaire, statut, sensibilité), dans ce contexte (heure, IP, canal) ?" L'exemple le plus parlant : "un utilisateur peut modifier un ticket" en RBAC devient "un utilisateur peut modifier un ticket **dont il est l'auteur**, ou dont il est assigné, ou qui appartient à son équipe" en ABAC — la permission dépend d'une comparaison entre attributs de l'utilisateur et attributs de la ressource, pas d'un rôle statique.

## 🛠️ Mise en œuvre
Keycloak propose Authorization Services (policies basées sur les attributs, evaluated côté serveur), mais pour une règle relativement simple, l'implémenter directement dans le code métier reste souvent plus lisible et plus facile à tester qu'une externalisation complète vers un moteur de policy :

```java
@PreAuthorize("hasRole('ADMIN') or @ticketSecurity.isOwner(#ticketId, authentication)")
public void update(Long ticketId, TicketUpdateRequest request) { /* ... */ }
```
```java
@Component("ticketSecurity")
class TicketSecurity {
    private final TicketRepository tickets;

    boolean isOwner(Long ticketId, Authentication auth) {
        return tickets.findById(ticketId)
                .map(t -> t.getOwnerUsername().equals(auth.getName()))
                .orElse(false);
    }
}
```
Pour des règles plus nombreuses et combinables (multi-tenant + statut + rôle + propriétaire), un moteur de policy externalisé (Open Policy Agent, ou les Authorization Services de Keycloak) devient plus lisible qu'une accumulation de `@PreAuthorize` complexes — le seuil de bascule est atteint dès que les règles commencent à se dupliquer entre plusieurs endpoints avec de légères variations.

## ❌ Erreurs classiques
- Réimplémenter ABAC "à la main" dans chaque service sans centraliser la logique de décision → chaque endroit du code réinvente sa propre version de la règle, et une correction de sécurité doit être répliquée partout au lieu d'un seul endroit.
- Charger la ressource complète juste pour vérifier un attribut d'autorisation, sans se soucier du coût (requête base de données supplémentaire sur chaque appel) → viable à faible volume, devient un vrai problème de performance à l'échelle si la vérification se fait sur un chemin critique à fort trafic.
- Faire dépendre la décision d'un attribut falsifiable côté client (un champ du body de la requête plutôt qu'une donnée relue depuis la base ou le token) → l'attribut décisif doit toujours venir d'une source de confiance (base de données, JWT signé), jamais d'un champ arbitraire envoyé par l'appelant.

## ✅ Vérification
Test qui vérifie explicitement le cas négatif le plus révélateur d'ABAC : un utilisateur authentifié, avec le bon rôle, mais qui n'est PAS propriétaire de la ressource, doit être refusé — c'est le scénario que RBAC seul laisserait passer.
```java
mockMvc.perform(put("/tickets/1").with(jwt().jwt(j -> j.claim("preferred_username", "autre-user"))))
       .andExpect(status().isForbidden());
```

## 🔗 Liens
- [rbac.md](rbac.md) — le socle plus simple qu'ABAC vient compléter, pas remplacer entièrement
- [oauth2-keycloak.md](oauth2-keycloak.md) — Authorization Services pour externaliser ces règles hors du code applicatif
