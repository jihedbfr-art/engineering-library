import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Random r = new Random();
        String color = String.format("#%06X", r.nextInt(0xFFFFFF + 1));
        System.out.println(color);
    }
}
