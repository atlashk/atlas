package org.atlas.infrastructure.file.excel.easyexcel.core;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EasyExcelReader {

  public static <T> List<T> read(byte[] fileContent, String sheetName, Class<T> beanClass) throws IOException {
    List<T> records = new ArrayList<>();
    try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
      // By default, 100 pieces of data will be read each time and then returned. Just call and use the data directly.
      // The specific number of rows to be returned can be set in the constructor of `PageReadListener`
      EasyExcel.read(inputStream, beanClass,
              new PageReadListener<>((Consumer<List<T>>) records::addAll))
          .sheet(sheetName)
          .doRead();
      return records;
    }
  }

  public static <T> void read(byte[] fileContent, String sheetName, Class<T> beanClass,
      int chunkSize, Consumer<List<T>> chunkConsumer) throws IOException {
    try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
      EasyExcel.read(inputStream, beanClass, new PageReadListener<>(chunkConsumer, chunkSize))
          .sheet(sheetName)
          .doRead();
    }
  }
}
