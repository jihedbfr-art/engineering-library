import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class ChangelogGenerator {
    public static void main(String[] args) throws Exception {
        Process proc = Runtime.getRuntime().exec(new String[]{"git", "log", "--pretty=format:%s"});
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (String type : List.of("feat", "fix", "docs", "chore", "refactor", "other")) {
            grouped.put(type, new ArrayList<>());
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String type = "other";
                for (String t : grouped.keySet()) {
                    if (line.startsWith(t + ":") || line.startsWith(t + "(")) {
                        type = t;
                        break;
                    }
                }
                grouped.get(type).add(line);
            }
        }
        grouped.forEach((type, commits) -> {
            if (!commits.isEmpty()) {
                System.out.println("## " + type);
                commits.forEach(c -> System.out.println("- " + c));
            }
        });
    }
}
