package org.atlas.common.framework.cryptography;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EncryptionUtil {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String KEY_ALGORITHM = "AES";
  private static final int IV_SIZE = 12; // GCM recommended
  private static final int TAG_SIZE = 128; // in bits

  public static String encrypt(String data, String secretKey) throws Exception {
    if (data == null) {
      return null;
    }

    byte[] iv = new byte[IV_SIZE];
    SecureRandom random = new SecureRandom();
    random.nextBytes(iv);

    SecretKeySpec key = createAESKey(secretKey);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
    cipher.init(Cipher.ENCRYPT_MODE, key, spec);

    byte[] cipherText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

    ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
    byteBuffer.put(iv);
    byteBuffer.put(cipherText);

    return Base64.getEncoder().encodeToString(byteBuffer.array());
  }

  public static String decrypt(String encryptedData, String secretKey) throws Exception {
    if (encryptedData == null) {
      return null;
    }

    byte[] decoded = Base64.getDecoder().decode(encryptedData);

    ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
    byte[] iv = new byte[IV_SIZE];
    byteBuffer.get(iv);
    byte[] cipherText = new byte[byteBuffer.remaining()];
    byteBuffer.get(cipherText);

    SecretKeySpec key = createAESKey(secretKey);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
    cipher.init(Cipher.DECRYPT_MODE, key, spec);

    byte[] plainText = cipher.doFinal(cipherText);
    return new String(plainText, StandardCharsets.UTF_8);
  }

  private static SecretKeySpec createAESKey(String secretKey) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
    return new SecretKeySpec(hash, KEY_ALGORITHM);
  }
}

