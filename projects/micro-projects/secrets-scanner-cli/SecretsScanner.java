import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SecretsScanner {
    static final List<Pattern> PATTERNS = List.of(
        Pattern.compile("AKIA[0-9A-Z]{16}"),
        Pattern.compile("(?i)api[_-]?key['\"]?\s*[:=]\s*['\"][a-zA-Z0-9]{16,}['\"]"),
        Pattern.compile("-----BEGIN (RSA|EC|OPENSSH) PRIVATE KEY-----")
    );

    public static void main(String[] args) throws IOException {
        Path root = Paths.get(args.length > 0 ? args[0] : ".");
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile).forEach(SecretsScanner::scan);
        }
    }

    static void scan(Path file) {
        try {
            List<String> lines = Files.readAllLines(file);
            for (int i = 0; i < lines.size(); i++) {
                for (Pattern p : PATTERNS) {
                    if (p.matcher(lines.get(i)).find()) {
                        System.out.println(file + ":" + (i + 1) + " -> secret potentiel detecte");
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
