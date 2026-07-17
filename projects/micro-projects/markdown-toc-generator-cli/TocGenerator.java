import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TocGenerator {
    static String slugify(String heading) {
        return heading.toLowerCase().trim().replaceAll("[^a-z0-9\s-]", "").replaceAll("\s+", "-");
    }

    public static void main(String[] args) throws IOException {
        String path = args.length > 0 ? args[0] : "README.md";
        List<String> lines = Files.readAllLines(Paths.get(path));
        StringBuilder toc = new StringBuilder("## Table des matieres\n\n");
        for (String line : lines) {
            if (line.startsWith("#")) {
                int level = 0;
                while (level < line.length() && line.charAt(level) == '#') level++;
                if (level > 1) {
                    String text = line.substring(level).trim();
                    String indent = "  ".repeat(level - 2);
                    toc.append(indent).append("- [").append(text).append("](#").append(slugify(text)).append(")\n");
                }
            }
        }
        System.out.println(toc);
    }
}
