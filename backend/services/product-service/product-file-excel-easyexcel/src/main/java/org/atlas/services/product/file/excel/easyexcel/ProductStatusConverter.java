package org.atlas.services.product.file.excel.easyexcel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;
import org.atlas.libs.framework.domain.product.ProductStatus;

public class ProductStatusConverter implements Converter<ProductStatus> {

  @Override
  public CellDataTypeEnum supportExcelTypeKey() {
    return CellDataTypeEnum.STRING;
  }

  @Override
  public WriteCellData<?> convertToExcelData(WriteConverterContext<ProductStatus> context)
      throws Exception {
    ProductStatus status = context.getValue();
    return new WriteCellData<>(status.name());
  }
}
