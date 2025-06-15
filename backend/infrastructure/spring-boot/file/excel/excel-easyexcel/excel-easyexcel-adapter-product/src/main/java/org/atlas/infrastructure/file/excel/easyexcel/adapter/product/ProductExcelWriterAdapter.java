package org.atlas.infrastructure.file.excel.easyexcel.adapter.product;

import com.alibaba.excel.annotation.ExcelProperty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.atlas.domain.product.port.file.excel.ProductExcelWriterPort;
import org.atlas.domain.product.port.file.model.write.ProductRow;
import org.atlas.domain.product.shared.enums.ProductStatus;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.file.excel.easyexcel.core.EasyExcelWriter;
import org.springframework.stereotype.Component;

@Component
public class ProductExcelWriterAdapter implements ProductExcelWriterPort {

  @Override
  public byte[] write(List<ProductRow> productRows) throws Exception {
    List<ProductExcelRow> csvRows = ObjectMapperUtil.getInstance()
        .mapList(productRows, ProductExcelRow.class);
    return EasyExcelWriter.write(csvRows, SHEET_NAME, ProductExcelRow.class);
  }

  @Data
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

    @ExcelProperty(value = "Description")
    private String description;

    @ExcelProperty(value = "Attribute Name 1")
    private String attributeName1;

    @ExcelProperty(value = "Attribute Value 1")
    private String attributeValue1;

    @ExcelProperty(value = "Attribute Name 2")
    private String attributeName2;

    @ExcelProperty(value = "Attribute Value 2")
    private String attributeValue2;

    @ExcelProperty(value = "Attribute Name 3")
    private String attributeName3;

    @ExcelProperty(value = "Attribute Value 3")
    private String attributeValue3;

    @ExcelProperty(value = "Branch ID")
    private Integer brandId;

    @ExcelProperty(value = "Category IDs")
    private String categoryIds;
  }
}
