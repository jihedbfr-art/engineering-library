import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversion Markdown -> HTML maison, sous-ensemble volontairement simple :
 * titres (# ##), gras (**x**), italique (*x*), paragraphes, listes à puces (- x).
 * Pas de support des liens, images, code blocks, tableaux, etc.
 */
public class MarkdownConverter {

    static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
    static final Pattern ITALIC = Pattern.compile("\\*(.+?)\\*");

    public static String toHtml(String markdown) {
        String[] lines = markdown.replace("\r\n", "\n").split("\n");
        StringBuilder html = new StringBuilder();
        List<String> paragraphBuffer = new ArrayList<>();
        List<String> listBuffer = new ArrayList<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.startsWith("- ")) {
                flushParagraph(html, paragraphBuffer);
                listBuffer.add(line.substring(2).trim());
                continue;
            } else {
                flushList(html, listBuffer);
            }

            if (line.isEmpty()) {
                flushParagraph(html, paragraphBuffer);
            } else if (line.startsWith("## ")) {
                flushParagraph(html, paragraphBuffer);
                html.append("<h2>").append(inline(line.substring(3))).append("</h2>\n");
            } else if (line.startsWith("# ")) {
                flushParagraph(html, paragraphBuffer);
                html.append("<h1>").append(inline(line.substring(2))).append("</h1>\n");
            } else {
                paragraphBuffer.add(line);
            }
        }
        flushList(html, listBuffer);
        flushParagraph(html, paragraphBuffer);
        return html.toString();
    }

    static void flushParagraph(StringBuilder html, List<String> buffer) {
        if (buffer.isEmpty()) return;
        html.append("<p>").append(inline(String.join(" ", buffer))).append("</p>\n");
        buffer.clear();
    }

    static void flushList(StringBuilder html, List<String> items) {
        if (items.isEmpty()) return;
        html.append("<ul>\n");
        for (String item : items) {
            html.append("<li>").append(inline(item)).append("</li>\n");
        }
        html.append("</ul>\n");
        items.clear();
    }

    static String inline(String text) {
        String escaped = BlogServer.escapeHtml(text);
        Matcher boldMatcher = BOLD.matcher(escaped);
        String withBold = boldMatcher.replaceAll("<strong>$1</strong>");
        Matcher italicMatcher = ITALIC.matcher(withBold);
        return italicMatcher.replaceAll("<em>$1</em>");
    }
}
