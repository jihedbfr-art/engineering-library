import java.util.List;

public class SqlFormatter {
    static final List<String> KEYWORDS = List.of(
        "SELECT", "FROM", "WHERE", "AND", "OR", "GROUP BY", "ORDER BY", "JOIN", "LEFT JOIN", "INNER JOIN", "LIMIT"
    );

    static String format(String sql) {
        String result = sql.replaceAll("\s+", " ").trim();
        for (String kw : KEYWORDS) {
            result = result.replaceAll("(?i)\b" + kw.replace(" ", "\\s+") + "\b", "\n" + kw);
        }
        return result.trim();
    }

    public static void main(String[] args) {
        String sql = args.length > 0 ? String.join(" ", args)
            : "select id, name from users where active = true and age > 18 order by name";
        System.out.println(format(sql));
    }
}
