package org.atlas.infrastructure.file.excel.easyexcel.adapter.product;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;
import org.atlas.domain.product.shared.enums.ProductStatus;

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
