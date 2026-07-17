import java.nio.file.Files;
import java.nio.file.Paths;

public class TextStatistics {
    public static void main(String[] args) throws Exception {
        String text = Files.readString(Paths.get(args[0]));
        int words = text.trim().isEmpty() ? 0 : text.trim().split("\s+").length;
        int sentences = text.split("[.!?]+").length;
        int chars = text.length();
        double readingMinutes = words / 200.0;

        System.out.println("Caracteres : " + chars);
        System.out.println("Mots       : " + words);
        System.out.println("Phrases    : " + sentences);
        System.out.printf("Lecture    : ~%.1f min (200 mots/min)%n", readingMinutes);
    }
}
