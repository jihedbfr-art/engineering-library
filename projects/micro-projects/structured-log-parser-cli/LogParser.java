import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {
    static final Pattern LOG_PATTERN = Pattern.compile(
        "^(?<time>\S+)\s+(?<level>\w+)\s+(?<message>.*)$"
    );

    public static void main(String[] args) throws Exception {
        Map<String, Integer> levelCounts = new HashMap<>();
        for (String line : Files.readAllLines(Paths.get(args[0]))) {
            Matcher m = LOG_PATTERN.matcher(line);
            if (m.matches()) {
                levelCounts.merge(m.group("level"), 1, Integer::sum);
            }
        }
        levelCounts.forEach((level, count) -> System.out.println(level + ": " + count));
    }
}
