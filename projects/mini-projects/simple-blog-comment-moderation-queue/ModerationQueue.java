import java.util.*;

public class ModerationQueue {
    enum Status { PENDING, APPROVED, REJECTED }

    record Comment(int id, String author, String text, Status status) {}

    private final List<Comment> comments = new ArrayList<>();
    private int nextId = 1;

    static final Set<String> BANNED_WORDS = Set.of("spam", "arnaque", "gratuit-cliquez-ici");

    public Comment submit(String author, String text) {
        boolean flagged = BANNED_WORDS.stream().anyMatch(word -> text.toLowerCase().contains(word));
        Status status = flagged ? Status.PENDING : Status.APPROVED;
        Comment comment = new Comment(nextId++, author, text, status);
        comments.add(comment);
        return comment;
    }

    public List<Comment> pending() {
        return comments.stream().filter(c -> c.status() == Status.PENDING).toList();
    }

    public static void main(String[] args) {
        ModerationQueue queue = new ModerationQueue();
        System.out.println(queue.submit("Alice", "Super article, merci !"));
        System.out.println(queue.submit("Bot123", "Offre gratuit-cliquez-ici maintenant"));
        System.out.println("En attente de moderation : " + queue.pending());
    }
}
