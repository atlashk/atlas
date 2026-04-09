package org.atlas.services.catalog.infrastructure.file.excel.easyexcel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;
import org.atlas.services.catalog.domain.entity.ProductType;

public class ProductTypeConverter implements Converter<ProductType> {

  @Override
  public CellDataTypeEnum supportExcelTypeKey() {
    return CellDataTypeEnum.STRING;
  }

  @Override
  public WriteCellData<?> convertToExcelData(WriteConverterContext<ProductType> context)
      throws Exception {
    ProductType type = context.getValue();
    return new WriteCellData<>(type.name());
  }
}
