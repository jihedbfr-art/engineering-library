import java.util.*;

public class SearchEngine {
    private final Map<String, Set<Integer>> index = new HashMap<>();
    private final List<String> documents = new ArrayList<>();

    public void addDocument(String text) {
        int docId = documents.size();
        documents.add(text);
        for (String word : tokenize(text)) {
            index.computeIfAbsent(word, k -> new TreeSet<>()).add(docId);
        }
    }

    private List<String> tokenize(String text) {
        return Arrays.asList(text.toLowerCase().split("\W+"));
    }

    public Set<Integer> search(String query) {
        Set<Integer> result = null;
        for (String word : tokenize(query)) {
            Set<Integer> docs = index.getOrDefault(word, Set.of());
            if (result == null) result = new TreeSet<>(docs);
            else result.retainAll(docs);
        }
        return result != null ? result : Set.of();
    }

    public static void main(String[] args) {
        SearchEngine engine = new SearchEngine();
        engine.addDocument("Spring Boot microservices architecture avec Keycloak");
        engine.addDocument("Kubernetes deploiement de microservices en production");
        engine.addDocument("Introduction au telecom et aux reseaux 5G");

        for (Integer docId : engine.search("microservices")) {
            System.out.println("Doc " + docId + " : " + engine.documents.get(docId));
        }
    }
}
