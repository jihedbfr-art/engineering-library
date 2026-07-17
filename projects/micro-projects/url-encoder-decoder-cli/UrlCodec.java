import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlCodec {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: UrlCodec <encode|decode> <texte>");
            return;
        }
        String mode = args[0];
        String text = args[1];
        if (mode.equals("encode")) {
            System.out.println(URLEncoder.encode(text, StandardCharsets.UTF_8));
        } else {
            System.out.println(URLDecoder.decode(text, StandardCharsets.UTF_8));
        }
    }
}
