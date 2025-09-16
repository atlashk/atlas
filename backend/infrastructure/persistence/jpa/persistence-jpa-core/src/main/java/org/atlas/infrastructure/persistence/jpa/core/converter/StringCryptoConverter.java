package org.atlas.infrastructure.persistence.jpa.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.cryptography.EncryptionUtil;
import org.atlas.framework.util.StringUtil;
import org.springframework.beans.factory.InitializingBean;

/**
 * JPA Attribute Converter for encrypting and decrypting String attributes. This converter uses the
 * Jasypt library for encryption and decryption operations. It is configured with an environment
 * variable for the encryption password.
 * <p>
 * Note: Ensure that the Jasypt library is correctly configured in your project. The encryption
 * password should be provided through the "jasypt.encryptor.password" property.
 *
 * @author rahul.chauhan
 */
@Converter
@RequiredArgsConstructor
public class StringCryptoConverter implements AttributeConverter<String, String>, InitializingBean {

  private final ApplicationConfigPort applicationConfigPort;

  private String encryptionKey;

  /**
   * Converts the attribute value to the encrypted form.
   *
   * @param attribute The original attribute value to be encrypted.
   * @return The encrypted form of the attribute.
   */
  @Override
  public String convertToDatabaseColumn(String attribute) {
    try {
      return EncryptionUtil.encrypt(attribute, encryptionKey);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts the encrypted database value to its decrypted form.
   *
   * @param dbData The encrypted value stored in the database.
   * @return The decrypted form of the database value.
   */
  @Override
  public String convertToEntityAttribute(String dbData) {
    try {
      return EncryptionUtil.decrypt(dbData, encryptionKey);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    encryptionKey = applicationConfigPort.getConfig(Application.SYSTEM, "encryption-key");
    if (StringUtil.isBlank(encryptionKey)) {
      throw new RuntimeException("encryptionKey not found");
    }
  }
}
