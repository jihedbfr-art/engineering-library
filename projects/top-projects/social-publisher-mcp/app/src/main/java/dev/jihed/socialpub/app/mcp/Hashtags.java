package dev.jihed.socialpub.app.mcp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Deterministic hashtag suggestion: tokenize, drop stopwords and short tokens, rank by frequency.
 * No external model call — this is honest keyword extraction, nothing more.
 */
final class Hashtags {

  private static final Pattern TOKEN = Pattern.compile("[^\\p{L}\\p{Nd}]+");
  private static final Set<String> STOPWORDS =
      Set.of(
          "the", "and", "for", "are", "but", "not", "you", "your", "with", "this", "that", "from",
          "have", "has", "was", "were", "will", "just", "about", "into", "over", "they", "them",
          "our", "out", "get", "got", "can", "all", "any", "how", "why", "who", "what", "when", "a",
          "an", "of", "to", "in", "on", "at", "is", "it", "as", "be", "by", "or", "we", "i");

  private Hashtags() {}

  static List<String> suggest(String caption, int count) {
    if (caption == null || caption.isBlank() || count <= 0) {
      return List.of();
    }
    Map<String, Integer> frequency = new LinkedHashMap<>();
    for (String raw : TOKEN.split(caption.toLowerCase())) {
      if (raw.length() < 3 || STOPWORDS.contains(raw) || raw.chars().allMatch(Character::isDigit)) {
        continue;
      }
      frequency.merge(raw, 1, Integer::sum);
    }
    List<String> ranked = new ArrayList<>(frequency.keySet());
    ranked.sort(
        Comparator.<String>comparingInt(frequency::get)
            .reversed()
            .thenComparing(Comparator.naturalOrder()));
    List<String> out = new ArrayList<>();
    for (String word : ranked) {
      if (out.size() >= count) {
        break;
      }
      out.add("#" + word);
    }
    return out;
  }
}
