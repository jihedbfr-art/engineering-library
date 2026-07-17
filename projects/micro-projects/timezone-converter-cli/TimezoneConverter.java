import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimezoneConverter {
    public static void main(String[] args) {
        String fromZone = args.length > 0 ? args[0] : "Europe/Paris";
        String toZone = args.length > 1 ? args[1] : "America/New_York";

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(fromZone));
        ZonedDateTime converted = now.withZoneSameInstant(ZoneId.of(toZone));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        System.out.println(fromZone + " : " + now.format(fmt));
        System.out.println(toZone + "   : " + converted.format(fmt));
    }
}
