package org.atlas.infrastructure.file.csv.opencsv.core;

import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OpenCsvReader {

  public static <T> List<T> read(byte[] fileContent, Class<T> beanClass) throws IOException {
    try (InputStream inputStream = new ByteArrayInputStream(fileContent);
        InputStreamReader reader = new InputStreamReader(inputStream)) {
      CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
          .withType(beanClass)
          .withSkipLines(1) // Skip the first line (header)
          .withIgnoreLeadingWhiteSpace(true)
          .build();
      return csvToBean.parse();
    }
  }

  public static <T> void read(byte[] fileContent, Class<T> beanClass, int batchSize,
      Consumer<List<T>> batchConsumer) throws IOException {
    List<T> batch = new ArrayList<>(batchSize);

    try (InputStream inputStream = new ByteArrayInputStream(fileContent);
        InputStreamReader reader = new InputStreamReader(inputStream);
        CSVReader csvReader = new CSVReader(reader)) {

      // Skip header
      csvReader.readNext();

      ColumnPositionMappingStrategy<T> strategy = new ColumnPositionMappingStrategy<>();
      strategy.setType(beanClass);

      CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(csvReader)
          .withMappingStrategy(strategy)
          .build();

      for (T record : csvToBean) {
        batch.add(record);

        if (batch.size() == batchSize) {
          batchConsumer.accept(new ArrayList<>(batch));
          batch.clear();
        }
      }

      // Process remaining records
      if (!batch.isEmpty()) {
        batchConsumer.accept(batch);
      }
    } catch (CsvValidationException e) {
      throw new RuntimeException(e);
    }
  }
}
