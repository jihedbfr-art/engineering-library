public class CronExplainer {
    public static void main(String[] args) {
        String expr = args.length > 0 ? args[0] : "0 9 * * 1-5";
        String[] parts = expr.split("\s+");
        if (parts.length != 5) {
            System.out.println("Format attendu : minute heure jour-du-mois mois jour-de-semaine");
            return;
        }
        System.out.println("Minute          : " + parts[0]);
        System.out.println("Heure           : " + parts[1]);
        System.out.println("Jour du mois    : " + parts[2]);
        System.out.println("Mois            : " + parts[3]);
        System.out.println("Jour de semaine : " + parts[4] + " (0/7=dimanche, 1-5=lundi-vendredi)");
    }
}
