package org.atlas.infrastructure.file.pdf.pdfbox.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.experimental.UtilityClass;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

@UtilityClass
public class PdfboxUtil {

  public static void setPassword(PDDocument document, String ownerPassword, String userPassword)
      throws IOException {
    AccessPermission accessPermission = new AccessPermission();
    accessPermission.setCanModify(false); // disallow modifications
    accessPermission.setCanPrint(true);   // allow printing

    StandardProtectionPolicy policy = new StandardProtectionPolicy(
        ownerPassword,
        userPassword,
        accessPermission
    );
    policy.setEncryptionKeyLength(128); // 128-bit encryption
    policy.setPermissions(accessPermission);

    document.protect(policy);
  }

  public static byte[] saveDocumentToBytes(PDDocument document) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      document.save(baos);
      return baos.toByteArray();
    }
  }
}
