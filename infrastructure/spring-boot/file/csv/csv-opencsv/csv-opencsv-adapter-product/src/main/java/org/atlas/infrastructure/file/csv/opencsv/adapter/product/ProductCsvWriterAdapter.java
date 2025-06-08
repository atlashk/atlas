package org.atlas.infrastructure.file.csv.opencsv.adapter.product;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.atlas.domain.product.port.file.csv.ProductCsvWriterPort;
import org.atlas.domain.product.port.file.model.write.ProductRow;
import org.atlas.domain.product.shared.enums.ProductStatus;
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

  @Data
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

    @CsvBindByName(column = "Description")
    @CsvBindByPosition(position = 7)
    private String description;

    @CsvBindByName(column = "Attribute Name 1")
    @CsvBindByPosition(position = 8)
    private String attributeName1;

    @CsvBindByName(column = "Attribute Value 1")
    @CsvBindByPosition(position = 9)
    private String attributeValue1;

    @CsvBindByName(column = "Attribute Name 1")
    @CsvBindByPosition(position = 10)
    private String attributeName2;

    @CsvBindByName(column = "Attribute Value 2")
    @CsvBindByPosition(position = 11)
    private String attributeValue2;

    @CsvBindByName(column = "Attribute Name 3")
    @CsvBindByPosition(position = 12)
    private String attributeName3;

    @CsvBindByName(column = "Attribute Value 3")
    @CsvBindByPosition(position = 13)
    private String attributeValue3;

    @CsvBindByName(column = "Branch ID")
    @CsvBindByPosition(position = 14)
    private Integer brandId;

    @CsvBindByName(column = "Category IDs")
    @CsvBindByPosition(position = 15)
    private String categoryIds;
  }
}
