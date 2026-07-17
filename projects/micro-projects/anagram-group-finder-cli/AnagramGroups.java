import java.util.*;

public class AnagramGroups {
    static Map<String, List<String>> group(List<String> words) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String word : words) {
            char[] chars = word.toLowerCase().toCharArray();
            Arrays.sort(chars);
            groups.computeIfAbsent(new String(chars), k -> new ArrayList<>()).add(word);
        }
        return groups;
    }

    public static void main(String[] args) {
        List<String> words = List.of("eat", "tea", "tan", "ate", "nat", "bat");
        for (var entry : group(words).values()) {
            if (entry.size() > 1) System.out.println(entry);
        }
    }
}
