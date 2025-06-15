package org.atlas.infrastructure.file.excel.poi.adapter.product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.atlas.domain.product.port.file.excel.ProductExcelReaderPort;
import org.atlas.domain.product.port.file.model.read.ProductRow;
import org.atlas.domain.product.shared.enums.ProductStatus;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.util.DateUtil;
import org.atlas.infrastructure.file.excel.poi.core.PoiUtil;
import org.springframework.stereotype.Component;

@Component
public class ProductExcelReaderAdapter implements ProductExcelReaderPort {

  private static final int BATCH_SIZE = 100;

  @Override
  public List<ProductRow> read(byte[] fileContent) throws IOException {
    try (InputStream inputStream = new ByteArrayInputStream(fileContent);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
      XSSFSheet sheet = workbook.getSheet(SHEET_NAME);
      List<ProductRow> excelRows = new ArrayList<>();
      int totalRows = sheet.getLastRowNum();
      int currentRow = 1; // Ignore header
      while (currentRow <= totalRows) {
        int endRow = Math.min(currentRow + BATCH_SIZE - 1, totalRows);
        for (int rowIndex = currentRow; rowIndex <= endRow; rowIndex++) {
          Row row = sheet.getRow(rowIndex);
          if (PoiUtil.isNotEmptyRow(row)) {
            ProductRow product = readRow(row);
            excelRows.add(product);
          }
        }
        currentRow += BATCH_SIZE;
      }
      return excelRows;
    }
  }

  private ProductRow readRow(Row row) {
    ProductRow productRow = new ProductRow();
    productRow.setName(row.getCell(0).getStringCellValue());
    productRow.setPrice(BigDecimal.valueOf(row.getCell(1).getNumericCellValue()));
    productRow.setQuantity((int) row.getCell(2).getNumericCellValue());
    productRow.setStatus(ProductStatus.valueOf(row.getCell(3).getStringCellValue()));
    productRow.setAvailableFrom(DateUtil.parse(row.getCell(4).getStringCellValue(),
        CommonConstant.DATE_TIME_FORMAT));
    productRow.setIsActive(row.getCell(5).getBooleanCellValue());
    productRow.setDescription(row.getCell(6).getStringCellValue());
    productRow.setAttributeName1(row.getCell(7).getStringCellValue());
    productRow.setAttributeValue1(row.getCell(8).getStringCellValue());
    productRow.setAttributeName2(row.getCell(9).getStringCellValue());
    productRow.setAttributeValue2(row.getCell(10).getStringCellValue());
    productRow.setAttributeName3(row.getCell(11).getStringCellValue());
    productRow.setAttributeValue3(row.getCell(12).getStringCellValue());
    productRow.setBrandId((int) row.getCell(13).getNumericCellValue());
    productRow.setCategoryIds(row.getCell(14).getStringCellValue());
    return productRow;
  }
}
