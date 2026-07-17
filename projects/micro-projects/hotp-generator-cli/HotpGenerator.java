import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

public class HotpGenerator {
    static String generate(byte[] key, long counter, int digits) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
        int otp = (int) (binary % Math.pow(10, digits));
        return String.format("%0" + digits + "d", otp);
    }

    public static void main(String[] args) throws Exception {
        String secret = args.length > 0 ? args[0] : "12345678901234567890";
        long counter = args.length > 1 ? Long.parseLong(args[1]) : 0;
        System.out.println(generate(secret.getBytes(), counter, 6));
    }
}
