package org.atlas.infrastructure.file.excel.easyexcel.impl.product;

import com.alibaba.excel.annotation.ExcelProperty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.product.infrastructure.file.excel.ProductExcelWriter;
import org.atlas.domain.product.infrastructure.file.model.write.ProductRow;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.file.excel.easyexcel.core.EasyExcelWriter;
import org.springframework.stereotype.Component;

@Component
public class EasyExcelProductExcelWriter implements ProductExcelWriter {

  @Override
  public byte[] write(List<ProductRow> productRows) throws Exception {
    List<ProductExcelRow> csvRows = ObjectMapperUtil.getInstance()
        .mapList(productRows, ProductExcelRow.class);
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

    @ExcelProperty(value = "Quantity")
    private Integer quantity;

    @ExcelProperty(value = "Status", converter = ProductStatusConverter.class)
    private ProductStatus status;

    @ExcelProperty(value = "Available From")
    private Date availableFrom;

    @ExcelProperty(value = "Active")
    private Boolean isActive;

    @ExcelProperty(value = "Branch ID")
    private Integer brandId;

    @ExcelProperty(value = "Category IDs")
    private String categoryIds;
  }
}
