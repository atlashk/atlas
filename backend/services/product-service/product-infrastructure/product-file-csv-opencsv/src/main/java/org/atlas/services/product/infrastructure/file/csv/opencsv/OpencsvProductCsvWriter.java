package org.atlas.services.product.infrastructure.file.csv.opencsv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.file.csv.opencsv.OpenCsvWriter;
import org.atlas.libs.framework.domain.product.ProductStatus;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.product.port.out.file.csv.ProductCsvWriter;
import org.atlas.services.product.port.out.file.model.ProductWriteRow;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
public class OpencsvProductCsvWriter implements ProductCsvWriter {

  @Override
  public byte[] write(List<ProductWriteRow> productRows) throws Exception {
    List<ProductCsvRow> csvRows = ObjectMapperUtil.mapList(productRows,
        ProductCsvRowMapper.INSTANCE::toProductCsvRow);
    return OpenCsvWriter.write(csvRows, ProductCsvRow.class);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
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

  @Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ProductCsvRowMapper {

    ProductCsvRowMapper INSTANCE = Mappers.getMapper(ProductCsvRowMapper.class);

    ProductCsvRow toProductCsvRow(ProductWriteRow productRow);
  }
}
