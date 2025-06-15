package org.atlas.infrastructure.file.excel.easyexcel.core;

import com.alibaba.excel.EasyExcel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EasyExcelWriter {

  public static <T> byte[] write(List<T> records, String sheetName, Class<T> beanClass)
      throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      EasyExcel.write(outputStream, beanClass)
          .sheet(sheetName)
          .doWrite(records);
      return outputStream.toByteArray();
    }
  }
}
