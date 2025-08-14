package org.atlas.framework.cryptography;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EncryptionUtil {

  private static final String ALGORITHM = "AES/GCM/NoPadding";

  public static String encrypt(String data, String secretKey) throws Exception {
    if (data == null) {
      return null;
    }
    SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encryptedBytes);
  }

  public static String decrypt(String encryptedData, String secretKey) throws Exception {
    if (encryptedData == null) {
      return null;
    }
    SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, key);
    byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
    return new String(decryptedBytes, StandardCharsets.UTF_8);
  }
}
