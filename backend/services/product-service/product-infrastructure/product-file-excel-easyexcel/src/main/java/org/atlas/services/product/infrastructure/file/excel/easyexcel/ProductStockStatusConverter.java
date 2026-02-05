package org.atlas.services.product.infrastructure.file.excel.easyexcel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;
import org.atlas.libs.framework.domain.product.ProductStockStatus;

public class ProductStockStatusConverter implements Converter<ProductStockStatus> {

  @Override
  public CellDataTypeEnum supportExcelTypeKey() {
    return CellDataTypeEnum.STRING;
  }

  @Override
  public WriteCellData<?> convertToExcelData(WriteConverterContext<ProductStockStatus> context)
      throws Exception {
    ProductStockStatus stockStatus = context.getValue();
    return new WriteCellData<>(stockStatus.name());
  }
}
