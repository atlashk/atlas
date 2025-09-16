package org.atlas.infrastructure.file.csv.opencsv.core;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OpenCsvWriter {

  public static <T> byte[] write(List<T> records, Class<T> beanClass) throws Exception {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream)) {
      // Header
      String header = buildHeader(beanClass);
      writer.append(header).append("\n");

      // Rows
      StatefulBeanToCsvBuilder<T> beanToCsvBuilder = new StatefulBeanToCsvBuilder<T>(writer)
          .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER);
      StatefulBeanToCsv<T> beanToCsv = beanToCsvBuilder.build();
      beanToCsv.write(records);

      // Flush the writer to ensure all data is written to the ByteArrayOutputStream
      writer.flush();

      return outputStream.toByteArray();
    }
  }

  private static String buildHeader(Class<?> beanClass) {
    return Arrays.stream(beanClass.getDeclaredFields())
        .filter(f -> f.getAnnotation(CsvBindByPosition.class) != null
            && f.getAnnotation(CsvBindByName.class) != null)
        .sorted(Comparator.comparing(f -> f.getAnnotation(CsvBindByPosition.class).position()))
        .map(f -> f.getAnnotation(CsvBindByName.class).column())
        .collect(Collectors.joining(","));
  }
}
