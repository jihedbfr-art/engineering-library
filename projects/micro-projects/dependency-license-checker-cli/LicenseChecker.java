import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenseChecker {
    static final Set<String> COPYLEFT_HINTS = Set.of("gpl", "agpl", "lgpl");
    static final Pattern LICENSE_LINE = Pattern.compile("\"license\"\s*:\s*\"([^\"]+)\"");

    public static void main(String[] args) throws IOException {
        String path = args.length > 0 ? args[0] : "package.json";
        String content = Files.readString(Paths.get(path));
        Matcher m = LICENSE_LINE.matcher(content);
        while (m.find()) {
            String license = m.group(1);
            boolean risky = COPYLEFT_HINTS.stream().anyMatch(hint -> license.toLowerCase().contains(hint));
            System.out.println(license + (risky ? "  <-- copyleft, verifier la compatibilite" : ""));
        }
    }
}
