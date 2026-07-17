public class Main {
    static boolean isValidTriangle(double a, double b, double c) {
        return a + b > c && b + c > a && a + c > b;
    }

    public static void main(String[] args) {
        double a = args.length > 0 ? Double.parseDouble(args[0]) : 3;
        double b = args.length > 1 ? Double.parseDouble(args[1]) : 4;
        double c = args.length > 2 ? Double.parseDouble(args[2]) : 5;
        System.out.println(isValidTriangle(a, b, c) ? "Triangle valide" : "Triangle impossible");
    }
}
