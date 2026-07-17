import java.io.IOException;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkChecker {
    static final Pattern LINK = Pattern.compile("\[[^\]]*\]\(([^)]+)\)");

    public static void main(String[] args) throws IOException {
        Path file = Paths.get(args.length > 0 ? args[0] : "README.md");
        Path baseDir = file.toAbsolutePath().getParent();
        String content = Files.readString(file);
        Matcher m = LINK.matcher(content);
        int broken = 0;
        while (m.find()) {
            String target = m.group(1);
            if (target.startsWith("http") || target.startsWith("#")) continue;
            Path resolved = baseDir.resolve(target.split("#")[0]);
            if (!Files.exists(resolved)) {
                System.out.println("Lien casse: " + target);
                broken++;
            }
        }
        System.out.println(broken + " lien(s) relatif(s) casse(s).");
    }
}
