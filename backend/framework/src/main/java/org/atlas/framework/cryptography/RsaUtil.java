package org.atlas.framework.cryptography;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RsaUtil {

  private static final String RSA_ALGORITHM = "RSA";
  private static final int KEY_SIZE = 2048;

  public static void generate(String publicKeyPath, String privateKeyPath)
      throws IOException, NoSuchAlgorithmException {

    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
    keyPairGenerator.initialize(KEY_SIZE);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();

    Base64.Encoder encoder = Base64.getEncoder();

    writeKeyToFile(publicKeyPath, encoder.encodeToString(keyPair.getPublic().getEncoded()),
        "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");

    writeKeyToFile(privateKeyPath, encoder.encodeToString(keyPair.getPrivate().getEncoded()),
        "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
  }

  private static void writeKeyToFile(String path, String base64EncodedKey, String header,
      String footer)
      throws IOException {
    try (Writer writer = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
      writer.write(header);
      writer.write("\n");
      writer.write(base64EncodedKey);
      writer.write("\n");
      writer.write(footer);
      writer.write("\n");
    }
  }
}
