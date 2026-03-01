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
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.domain.entity.ProductType;
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

    @CsvBindByName(column = "Type")
    @CsvBindByPosition(position = 1)
    private ProductType type;
    
    @CsvBindByName(column = "Price")
    @CsvBindByPosition(position = 2)
    private BigDecimal price;

    @CsvBindByName(column = "Publish Date")
    @CsvBindByPosition(position = 3)
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    private Date publishedAt;

    @CsvBindByName(column = "Quantity")
    @CsvBindByPosition(position = 4)
    private Integer initialQuantity;

    @CsvBindByName(column = "Branch ID")
    @CsvBindByPosition(position = 5)
    private String brandId;

    @CsvBindByName(column = "Category IDs")
    @CsvBindByPosition(position = 6)
    private String categoryIds;
  }

  @Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ProductCsvRowMapper {

    ProductCsvRowMapper INSTANCE = Mappers.getMapper(ProductCsvRowMapper.class);

    ProductReadRow toProductRow(ProductCsvRow productCsvRow);
  }
}
