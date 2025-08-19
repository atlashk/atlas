package org.atlas.framework.cryptography;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EncryptionUtilTest {

  private static final String SECRET_KEY = "123456";

  @Test
  void testEncrypt() throws Exception {
    String originalData = "admin@atlas.org";
    String encryptedData = EncryptionUtil.encrypt(originalData, SECRET_KEY);
    System.out.println(encryptedData);
    assertNotNull(encryptedData);
    assertNotEquals(originalData, encryptedData);
    assertTrue(encryptedData.length() > 0);
  }
}
