import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class LogFilter {
    static final List<String> ORDER = List.of("DEBUG", "INFO", "WARN", "ERROR");

    public static void main(String[] args) throws IOException {
        String path = args.length > 0 ? args[0] : "app.log";
        String minLevel = args.length > 1 ? args[1].toUpperCase() : "WARN";
        int threshold = ORDER.indexOf(minLevel);

        for (String line : Files.readAllLines(Paths.get(path))) {
            for (int i = ORDER.size() - 1; i >= threshold; i--) {
                if (line.contains(ORDER.get(i))) {
                    System.out.println(line);
                    break;
                }
            }
        }
    }
}
