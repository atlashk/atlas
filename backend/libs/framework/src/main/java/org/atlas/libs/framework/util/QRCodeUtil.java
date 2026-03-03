package org.atlas.libs.framework.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QRCodeUtil {

  private static final int DEFAULT_WIDTH = 200;
  private static final int DEFAULT_HEIGHT = 200;
  private static final String DEFAULT_IMAGE_FORMAT = "png";
  
  public static byte[] generate(String text) throws IOException, WriterException {
    return generate(text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  public static byte[] generate(String text, int width, int height) throws WriterException, IOException {
    Map<EncodeHintType, Object> hints = new HashMap<>();
    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
    BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height,
        hints);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(bitMatrix, DEFAULT_IMAGE_FORMAT, byteArrayOutputStream);
    return byteArrayOutputStream.toByteArray();
  }
}
