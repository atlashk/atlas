package org.atlas.framework.cryptography;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.experimental.UtilityClass;
import org.atlas.framework.util.FileUtil;

@UtilityClass
public class RsaKeyLoader {

  private static final String RSA_ALGORITHM = "RSA";
  private static final String PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----";
  private static final String PUBLIC_KEY_FOOTER = "-----END PUBLIC KEY-----";
  private static final String PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
  private static final String PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";

  private static final KeyFactory keyFactory;

  private static volatile RSAPublicKey publicKey = null;
  private static volatile RSAPrivateKey privateKey = null;

  static {
    try {
      keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("RSA algorithm not found", e);
    }
  }

  public static RSAPublicKey loadPublicKey(String publicKeyPath) throws IOException, InvalidKeySpecException {
    if (publicKey == null) {
      synchronized (RsaKeyLoader.class) {
        if (publicKey == null) {
          byte[] keyBytes = readKeyBytes(publicKeyPath, PUBLIC_KEY_HEADER, PUBLIC_KEY_FOOTER);
          X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
          publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        }
      }
    }
    return publicKey;
  }

  public static RSAPrivateKey loadPrivateKey(String privateKeyPath) throws IOException, InvalidKeySpecException {
    if (privateKey == null) {
      synchronized (RsaKeyLoader.class) {
        if (privateKey == null) {
          byte[] keyBytes = readKeyBytes(privateKeyPath, PRIVATE_KEY_HEADER, PRIVATE_KEY_FOOTER);
          PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
          privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        }
      }
    }
    return privateKey;
  }

  private static byte[] readKeyBytes(String resource, String header, String footer) throws IOException {
    try (InputStream inputStream = FileUtil.readResourceFileAsStream(resource)) {
      String content = new String(inputStream.readAllBytes())
          .replace(header, "")
          .replace(footer, "")
          .replaceAll("\\s+", "");
      return Base64.getDecoder().decode(content);
    }
  }
}
