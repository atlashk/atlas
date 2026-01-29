package org.atlas.services.product.file.excel.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.file.excel.easyexcel.EasyExcelReader;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.domain.product.ProductStatus;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.product.application.port.file.excel.ProductExcelReader;
import org.atlas.services.product.application.port.file.model.ProductReadRow;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
public class EasyExcelProductExcelReader implements ProductExcelReader {

  @Override
  public List<ProductReadRow> read(byte[] fileContent) throws IOException {
    List<ProductExcelRow> excelRows =
        EasyExcelReader.read(fileContent, SHEET_NAME, ProductExcelRow.class);
    return ObjectMapperUtil.mapList(excelRows, ProductExcelRowMapper.INSTANCE::toProductRow);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class ProductExcelRow {

    @ExcelProperty(value = "Name")
    private String name;

    @ExcelProperty(value = "Price")
    private BigDecimal price;

    @ExcelProperty(value = "Quantity")
    private Integer quantity;

    @ExcelProperty(value = "Status")
    private ProductStatus status;

    @ExcelProperty(value = "Available From")
    @DateTimeFormat(value = CommonConstant.DATE_TIME_FORMAT)
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

    ProductReadRow toProductRow(ProductExcelRow productExcelRow);
  }
}
