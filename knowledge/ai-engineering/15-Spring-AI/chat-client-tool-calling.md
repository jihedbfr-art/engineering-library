# Spring AI — ChatClient et Tool Calling

## 🎯 Objectif
Brancher un LLM dans une application Spring Boot existante via `ChatClient`, et lui donner accès
à des méthodes Java du domaine (tool calling) plutôt que de tout faire par prompt seul.

## 🧩 Contexte d'usage
Un service Spring Boot (ex. `notes-app-microservices`, `simple-crm`) doit exposer une fonctionnalité
« assistant » — résumé, recherche en langage naturel, action métier déclenchée par le langage —
sans réécrire toute la couche service. Spring AI permet d'exposer les `@Service` existants comme
tools appelables par le modèle.

## 🛠️ Recette
```java
@Configuration
class AiConfig {
    @Bean
    ChatClient chatClient(ChatClient.Builder builder, NoteTools noteTools) {
        return builder
            .defaultSystem("Tu es un assistant qui gère des notes utilisateur.")
            .defaultTools(noteTools)
            .build();
    }
}

@Component
class NoteTools {
    private final NoteRepository repo;
    NoteTools(NoteRepository repo) { this.repo = repo; }

    @Tool(description = "Recherche les notes d'un utilisateur par mot-clé")
    List<NoteDto> searchNotes(String userId, String keyword) {
        return repo.searchByUserAndKeyword(userId, keyword).stream().map(NoteDto::from).toList();
    }
}

// Appel
String reponse = chatClient.prompt()
    .user("Trouve mes notes qui parlent de Kafka")
    .call()
    .content();
```

## ✅ Résultat attendu
Le modèle décide seul quand appeler `searchNotes`, avec quels arguments, et compose la réponse
finale en langage naturel à partir du résultat structuré retourné par la méthode Java — sans que
le prompt ait à décrire manuellement le contenu de la base.

## ⚠️ Piège
- **Tool sans scope utilisateur** : si `userId` vient du prompt plutôt que du contexte de sécurité
  Spring Security authentifié, un prompt malveillant peut usurper un autre utilisateur — toujours
  dériver l'identité du `SecurityContext`, jamais d'un argument fourni au modèle.
- **Trop de tools d'un coup** : au-delà d'une dizaine de tools exposés simultanément, le taux de
  bonne sélection du tool chute — grouper par contexte d'usage (advisor différent selon l'écran).
- **Absence d'observabilité** : sans logging des appels de tools (cf. `13-Observability`),
  impossible de diagnostiquer pourquoi le modèle a pris une décision inattendue en prod.
