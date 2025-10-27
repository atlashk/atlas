package org.atlas.infrastructure.file.csv.opencsv.impl.product;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.product.infrastructure.file.csv.ProductCsvReader;
import org.atlas.domain.product.infrastructure.file.model.read.ProductRow;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.file.csv.opencsv.core.OpenCsvReader;
import org.springframework.stereotype.Component;

@Component
public class OpencsvProductCsvReader implements ProductCsvReader {

  @Override
  public List<ProductRow> read(byte[] fileContent) throws IOException {
    List<ProductCsvRow> csvRows = OpenCsvReader.read(fileContent, ProductCsvRow.class);
    return ObjectMapperUtil.getInstance()
        .mapList(csvRows, ProductRow.class);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class ProductCsvRow {

    @CsvBindByName(column = "Name")
    @CsvBindByPosition(position = 0)
    private String name;

    @CsvBindByName(column = "Price")
    @CsvBindByPosition(position = 1)
    private BigDecimal price;

    @CsvBindByName(column = "Quantity")
    @CsvBindByPosition(position = 2)
    private Integer quantity;

    @CsvBindByName(column = "Status")
    @CsvBindByPosition(position = 3)
    private ProductStatus status;

    @CsvBindByName(column = "Available From")
    @CsvBindByPosition(position = 4)
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    private Date availableFrom;

    @CsvBindByName(column = "Active")
    @CsvBindByPosition(position = 5)
    private Boolean isActive;

    @CsvBindByName(column = "Branch ID")
    @CsvBindByPosition(position = 6)
    private Integer brandId;

    @CsvBindByName(column = "Category IDs")
    @CsvBindByPosition(position = 7)
    private String categoryIds;
  }
}
