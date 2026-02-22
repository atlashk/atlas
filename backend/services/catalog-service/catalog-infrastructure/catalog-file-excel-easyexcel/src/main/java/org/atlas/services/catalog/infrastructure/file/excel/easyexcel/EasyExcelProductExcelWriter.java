package org.atlas.services.catalog.infrastructure.file.excel.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.file.excel.easyexcel.EasyExcelWriter;
import org.atlas.libs.framework.domain.catalog.ProductType;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.port.out.file.excel.ProductExcelWriter;
import org.atlas.services.catalog.port.out.file.model.ProductWriteRow;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
public class EasyExcelProductExcelWriter implements ProductExcelWriter {

  @Override
  public byte[] write(List<ProductWriteRow> productRows) throws Exception {
    List<ProductExcelRow> csvRows = MapperUtil.mapList(productRows,
        ProductExcelRowMapper.INSTANCE::toProductExcelRow);
    return EasyExcelWriter.write(csvRows, SHEET_NAME, ProductExcelRow.class);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class ProductExcelRow {

    @ExcelProperty(value = "ID")
    private Integer id;

    @ExcelProperty(value = "Name")
    private String name;

    @ExcelProperty(value = "Price")
    private BigDecimal price;

    @ExcelProperty(value = "Stock Status", converter = ProductTypeConverter.class)
    private ProductType stockStatus;

    @ExcelProperty(value = "Quantity")
    private Integer quantity;

    @ExcelProperty(value = "Available From")
    private Date availableFrom;

    @ExcelProperty(value = "Active")
    private Boolean isActive;

    @ExcelProperty(value = "Branch ID")
    private Integer brandId;

    @ExcelProperty(value = "Category IDs")
    private String categoryIds;
  }

  @Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface ProductExcelRowMapper {

    ProductExcelRowMapper INSTANCE = Mappers.getMapper(ProductExcelRowMapper.class);

    ProductExcelRow toProductExcelRow(ProductWriteRow productExcelRow);
  }
}
