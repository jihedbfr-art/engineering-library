import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenApiLint {
    public static void main(String[] args) throws IOException {
        String path = args.length > 0 ? args[0] : "openapi.yaml";
        List<String> lines = Files.readAllLines(Paths.get(path));
        Pattern pathLine = Pattern.compile("^\s*(get|post|put|delete|patch):\s*$", Pattern.CASE_INSENSITIVE);
        int warnings = 0;
        for (int i = 0; i < lines.size(); i++) {
            Matcher m = pathLine.matcher(lines.get(i));
            if (m.matches()) {
                boolean hasDescription = (i + 1 < lines.size() && lines.get(i + 1).contains("description"))
                        || (i + 2 < lines.size() && lines.get(i + 2).contains("description"));
                if (!hasDescription) {
                    System.out.println("Ligne " + (i + 1) + ": operation '" + m.group(1) + "' sans description proche");
                    warnings++;
                }
            }
        }
        System.out.println(warnings + " avertissement(s).");
    }
}
