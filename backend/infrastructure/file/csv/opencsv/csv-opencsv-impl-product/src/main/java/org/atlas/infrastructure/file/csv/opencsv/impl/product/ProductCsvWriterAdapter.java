package org.atlas.infrastructure.file.csv.opencsv.impl.product;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.product.port.file.csv.ProductCsvWriterPort;
import org.atlas.domain.product.port.file.model.write.ProductRow;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.file.csv.opencsv.core.OpenCsvWriter;
import org.springframework.stereotype.Component;

@Component
public class ProductCsvWriterAdapter implements ProductCsvWriterPort {

  @Override
  public byte[] write(List<ProductRow> productRows) throws Exception {
    List<ProductCsvRow> csvRows = ObjectMapperUtil.getInstance()
        .mapList(productRows, ProductCsvRow.class);
    return OpenCsvWriter.write(csvRows, ProductCsvRow.class);
  }

  @Getter
  @Setter
  public static class ProductCsvRow {

    @CsvBindByName(column = "ID")
    @CsvBindByPosition(position = 0)
    private Integer id;

    @CsvBindByName(column = "Name")
    @CsvBindByPosition(position = 1)
    private String name;

    @CsvBindByName(column = "Price")
    @CsvBindByPosition(position = 2)
    private BigDecimal price;

    @CsvBindByName(column = "Quantity")
    @CsvBindByPosition(position = 3)
    private Integer quantity;

    @CsvBindByName(column = "Status")
    @CsvBindByPosition(position = 4)
    private ProductStatus status;

    @CsvBindByName(column = "Available From")
    @CsvBindByPosition(position = 5)
    private Date availableFrom;

    @CsvBindByName(column = "Active")
    @CsvBindByPosition(position = 6)
    private Boolean isActive;

    @CsvBindByName(column = "Branch ID")
    @CsvBindByPosition(position = 7)
    private Integer brandId;

    @CsvBindByName(column = "Category IDs")
    @CsvBindByPosition(position = 8)
    private String categoryIds;
  }
}
