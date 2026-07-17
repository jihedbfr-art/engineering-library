import java.util.Map;

public class GitignoreGenerator {
    static final Map<String, String> TEMPLATES = Map.of(
        "java", "target/\n*.class\n*.jar\n.idea/\n",
        "node", "node_modules/\ndist/\n.env\n",
        "python", "__pycache__/\n*.pyc\n.venv/\n",
        "angular", ".angular/\ndist/\nnode_modules/\n"
    );

    public static void main(String[] args) {
        String stack = args.length > 0 ? args[0].toLowerCase() : "java";
        String content = TEMPLATES.getOrDefault(stack, "# stack inconnue: " + stack + "\n");
        System.out.print(content);
    }
}
