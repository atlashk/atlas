package org.atlas.services.catalog.infrastructure.file.csv.opencsv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.file.csv.opencsv.OpenCsvWriter;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.domain.entity.ProductType;
import org.atlas.services.catalog.port.out.file.csv.ProductCsvWriter;
import org.atlas.services.catalog.port.out.file.model.ProductWriteRow;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
public class OpencsvProductCsvWriter implements ProductCsvWriter {

  @Override
  public byte[] write(List<ProductWriteRow> productRows) throws Exception {
    List<ProductCsvRow> csvRows = MapperUtil.mapList(productRows,
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

    @CsvBindByName(column = "Type")
    @CsvBindByPosition(position = 2)
    private ProductType type;

    @CsvBindByName(column = "Price")
    @CsvBindByPosition(position = 3)
    private BigDecimal price;

    @CsvBindByName(column = "Publish Date")
    @CsvBindByPosition(position = 4)
    private LocalDateTime publishedAt;

    @CsvBindByName(column = "In Stock")
    @CsvBindByPosition(position = 5)
    private Boolean inStock;

    @CsvBindByName(column = "Branch ID")
    @CsvBindByPosition(position = 6)
    private String brandId;

    @CsvBindByName(column = "Category IDs")
    @CsvBindByPosition(position = 7)
    private String categoryIds;
  }

  @Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ProductCsvRowMapper {

    ProductCsvRowMapper INSTANCE = Mappers.getMapper(ProductCsvRowMapper.class);

    ProductCsvRow toProductCsvRow(ProductWriteRow productRow);
  }
}
