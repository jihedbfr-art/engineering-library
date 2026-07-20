package dev.jihed.socialpub.app.credentials;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AES-GCM encryption for platform tokens at rest. The key comes from {@code
 * socialpub.credentials.enc-key} (base64, 16 or 32 bytes). The 12-byte IV is random per message and
 * prepended to the ciphertext.
 */
@Component
public class CredentialCipher {

  private static final int IV_BYTES = 12;
  private static final int TAG_BITS = 128;
  private static final SecureRandom RANDOM = new SecureRandom();

  private final byte[] key;

  public CredentialCipher(@Value("${socialpub.credentials.enc-key:}") String base64Key) {
    this.key =
        (base64Key == null || base64Key.isBlank()) ? null : Base64.getDecoder().decode(base64Key);
    if (key != null && key.length != 16 && key.length != 32) {
      throw new IllegalStateException(
          "socialpub.credentials.enc-key must decode to 16 or 32 bytes, got " + key.length);
    }
  }

  public byte[] encrypt(String plaintext) {
    requireKey();
    try {
      byte[] iv = new byte[IV_BYTES];
      RANDOM.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(
          Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      byte[] out = new byte[iv.length + ciphertext.length];
      System.arraycopy(iv, 0, out, 0, iv.length);
      System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);
      return out;
    } catch (Exception e) {
      throw new IllegalStateException("Encryption failed", e);
    }
  }

  public String decrypt(byte[] payload) {
    requireKey();
    try {
      byte[] iv = new byte[IV_BYTES];
      System.arraycopy(payload, 0, iv, 0, IV_BYTES);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(
          Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
      byte[] plaintext = cipher.doFinal(payload, IV_BYTES, payload.length - IV_BYTES);
      return new String(plaintext, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("Decryption failed", e);
    }
  }

  private void requireKey() {
    if (key == null) {
      throw new IllegalStateException(
          "No credentials encryption key configured (socialpub.credentials.enc-key)");
    }
  }
}
