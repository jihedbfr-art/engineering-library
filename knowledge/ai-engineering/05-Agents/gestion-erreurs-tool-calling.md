# Gestion des erreurs de tool calling dans un agent

## 🎯 Objectif
Faire en sorte qu'un agent gère proprement l'échec d'un tool (timeout, exception métier, donnée
introuvable) au lieu de soit planter, soit halluciner un résultat plausible à la place.

## 🧩 Contexte d'usage
Un agent qui appelle des tools Spring (cf. `15-Spring-AI`) va, tôt ou tard, appeler un tool qui
échoue : ressource absente, contrainte métier violée, service externe indisponible.

## 🛠️ Recette
- **Ne jamais laisser une exception Java brute remonter dans le contexte du modèle** : elle contient
  souvent des détails internes (stack trace, nom de classe) sans valeur pour la décision suivante
  du modèle, et peut fuiter de l'information technique sensible.
- **Retourner un résultat d'erreur structuré et actionnable** : le tool doit renvoyer une réponse
  que le modèle peut interpréter pour décider de la suite (réessayer, informer l'utilisateur,
  essayer un autre chemin), pas juste "erreur".

```java
@Tool(description = "Recherche une facture par numéro")
ToolResult findInvoice(String invoiceNumber) {
    try {
        Invoice invoice = invoiceService.find(invoiceNumber);
        return ToolResult.success(InvoiceDto.from(invoice));
    } catch (InvoiceNotFoundException e) {
        return ToolResult.failure("INVOICE_NOT_FOUND",
            "Aucune facture avec ce numéro. Vérifier l'orthographe ou demander le numéro client.");
    } catch (Exception e) {
        log.error("Erreur tool findInvoice", e);
        return ToolResult.failure("INTERNAL_ERROR", "Service momentanément indisponible.");
    }
}
```

- **Distinguer erreur métier (le modèle peut ajuster sa réponse) et erreur technique** (le modèle
  doit juste informer l'utilisateur, pas essayer de contourner).

## ✅ Résultat attendu
Un agent qui, face à un tool en échec, informe l'utilisateur de façon cohérente ("je ne trouve pas
cette facture, pouvez-vous vérifier le numéro ?") plutôt que d'inventer une réponse plausible ou de
planter toute la conversation.

## ⚠️ Piège
- **Masquer l'échec plutôt que le remonter** : si le tool renvoie une valeur par défaut silencieuse
  en cas d'erreur plutôt qu'un statut d'échec explicite, le modèle répondra avec une fausse
  confiance sur une donnée invalide.
- **Laisser le modèle réessayer indéfiniment** : sans limite explicite de tentatives, un agent face
  à un tool qui échoue systématiquement peut boucler et consommer du budget sans jamais aboutir —
  fixer une limite de retries stricte.
