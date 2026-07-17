public class Main {
    public static void main(String[] args) {
        double a = args.length > 0 ? Double.parseDouble(args[0]) : 1;
        double b = args.length > 1 ? Double.parseDouble(args[1]) : -3;
        double c = args.length > 2 ? Double.parseDouble(args[2]) : 2;
        double delta = b * b - 4 * a * c;
        if (delta < 0) {
            System.out.println("Pas de racine reelle (delta = " + delta + ")");
        } else {
            double x1 = (-b + Math.sqrt(delta)) / (2 * a);
            double x2 = (-b - Math.sqrt(delta)) / (2 * a);
            System.out.println("x1 = " + x1 + ", x2 = " + x2);
        }
    }
}
