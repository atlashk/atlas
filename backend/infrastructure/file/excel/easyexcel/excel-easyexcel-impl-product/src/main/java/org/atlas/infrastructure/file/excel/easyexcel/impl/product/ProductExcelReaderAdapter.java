package org.atlas.infrastructure.file.excel.easyexcel.impl.product;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.product.port.file.excel.ProductExcelReaderPort;
import org.atlas.domain.product.port.file.model.read.ProductRow;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.file.excel.easyexcel.core.EasyExcelReader;
import org.springframework.stereotype.Component;

@Component
public class ProductExcelReaderAdapter implements ProductExcelReaderPort {

  @Override
  public List<ProductRow> read(byte[] fileContent) throws IOException {
    List<ProductExcelRow> excelRows =
        EasyExcelReader.read(fileContent, SHEET_NAME, ProductExcelRow.class);
    return ObjectMapperUtil.getInstance()
        .mapList(excelRows, ProductRow.class);
  }

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
}
