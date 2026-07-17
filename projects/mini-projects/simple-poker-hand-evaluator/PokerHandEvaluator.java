import java.util.*;
import java.util.stream.*;

public class PokerHandEvaluator {
    static String evaluate(String[] cards) {
        List<Integer> ranks = Arrays.stream(cards).map(c -> rankValue(c.substring(0, c.length() - 1))).sorted().collect(Collectors.toList());
        List<Character> suits = Arrays.stream(cards).map(c -> c.charAt(c.length() - 1)).collect(Collectors.toList());

        boolean flush = new HashSet<>(suits).size() == 1;
        boolean straight = IntStream.range(0, 4).allMatch(i -> ranks.get(i + 1) - ranks.get(i) == 1);

        Map<Integer, Long> counts = ranks.stream().collect(Collectors.groupingBy(r -> r, Collectors.counting()));
        List<Long> countValues = counts.values().stream().sorted(Collections.reverseOrder()).toList();

        if (straight && flush) return "Quinte flush";
        if (countValues.get(0) == 4) return "Carre";
        if (countValues.get(0) == 3 && countValues.size() > 1 && countValues.get(1) == 2) return "Full";
        if (flush) return "Couleur";
        if (straight) return "Suite";
        if (countValues.get(0) == 3) return "Brelan";
        if (countValues.get(0) == 2 && countValues.size() > 1 && countValues.get(1) == 2) return "Double paire";
        if (countValues.get(0) == 2) return "Paire";
        return "Carte haute";
    }

    static int rankValue(String rank) {
        return switch (rank) {
            case "J" -> 11;
            case "Q" -> 12;
            case "K" -> 13;
            case "A" -> 14;
            default -> Integer.parseInt(rank);
        };
    }

    public static void main(String[] args) {
        System.out.println(evaluate(new String[]{"10H", "JH", "QH", "KH", "AH"}));
        System.out.println(evaluate(new String[]{"2C", "2D", "2H", "5S", "5C"}));
        System.out.println(evaluate(new String[]{"3C", "7D", "9H", "JS", "KC"}));
    }
}
