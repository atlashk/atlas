package org.atlas.infrastructure.file.excel.easyexcel.adapter.product;

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
import org.atlas.application.product.port.file.excel.ProductExcelReader;
import org.atlas.application.product.port.file.model.ProductReadRow;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.file.excel.easyexcel.core.EasyExcelReader;
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
