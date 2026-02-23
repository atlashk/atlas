package org.atlas.services.catalog.infrastructure.file.csv.opencsv;

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
import org.atlas.libs.file.csv.opencsv.OpenCsvReader;
import org.atlas.services.catalog.domain.entity.ProductType;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.port.out.file.csv.ProductCsvReader;
import org.atlas.services.catalog.port.out.file.model.ProductReadRow;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
public class OpencsvProductCsvReader implements ProductCsvReader {

  @Override
  public List<ProductReadRow> read(byte[] fileContent) throws IOException {
    List<ProductCsvRow> csvRows = OpenCsvReader.read(fileContent, ProductCsvRow.class);
    return MapperUtil.mapList(csvRows, ProductCsvRowMapper.INSTANCE::toProductRow);
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

    @CsvBindByName(column = "Stock Status")
    @CsvBindByPosition(position = 2)
    private ProductType stockStatus;

    @CsvBindByName(column = "Quantity")
    @CsvBindByPosition(position = 3)
    private Integer quantity;

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

  @Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ProductCsvRowMapper {

    ProductCsvRowMapper INSTANCE = Mappers.getMapper(ProductCsvRowMapper.class);

    ProductReadRow toProductRow(ProductCsvRow productCsvRow);
  }
}
