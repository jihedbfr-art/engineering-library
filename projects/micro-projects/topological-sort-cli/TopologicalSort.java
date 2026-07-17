import java.util.*;

public class TopologicalSort {
    static List<String> sort(Map<String, List<String>> graph) {
        Map<String, Integer> inDegree = new HashMap<>();
        for (String node : graph.keySet()) inDegree.putIfAbsent(node, 0);
        for (List<String> deps : graph.values()) {
            for (String d : deps) inDegree.merge(d, 1, Integer::sum);
        }
        Deque<String> queue = new ArrayDeque<>();
        for (var e : inDegree.entrySet()) if (e.getValue() == 0) queue.add(e.getKey());

        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String node = queue.poll();
            result.add(node);
            for (String dep : graph.getOrDefault(node, List.of())) {
                inDegree.merge(dep, -1, Integer::sum);
                if (inDegree.get(dep) == 0) queue.add(dep);
            }
        }
        if (result.size() != inDegree.size()) throw new IllegalStateException("Cycle detecte");
        return result;
    }

    public static void main(String[] args) {
        Map<String, List<String>> graph = new LinkedHashMap<>();
        graph.put("compile", List.of("test"));
        graph.put("test", List.of("package"));
        graph.put("package", List.of("deploy"));
        graph.put("lint", List.of("test"));
        System.out.println("Ordre d'execution : " + sort(graph));
    }
}
