package org.atlas.infrastructure.file.excel.poi.impl.product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.atlas.domain.product.infrastructure.file.excel.ProductExcelReader;
import org.atlas.domain.product.infrastructure.file.model.ProductReadRow;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.util.DateUtil;
import org.atlas.infrastructure.file.excel.poi.core.PoiUtil;
import org.springframework.stereotype.Component;

@Component
public class PoiProductExcelReader implements ProductExcelReader {

  private static final int BATCH_SIZE = 100;

  @Override
  public List<ProductReadRow> read(byte[] fileContent) throws IOException {
    try (InputStream inputStream = new ByteArrayInputStream(fileContent);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
      XSSFSheet sheet = workbook.getSheet(SHEET_NAME);
      List<ProductReadRow> excelRows = new ArrayList<>();
      int totalRows = sheet.getLastRowNum();
      int currentRow = 1; // Ignore header
      while (currentRow <= totalRows) {
        int endRow = Math.min(currentRow + BATCH_SIZE - 1, totalRows);
        for (int rowIndex = currentRow; rowIndex <= endRow; rowIndex++) {
          Row row = sheet.getRow(rowIndex);
          if (PoiUtil.isNotEmptyRow(row)) {
            ProductReadRow product = readRow(row);
            excelRows.add(product);
          }
        }
        currentRow += BATCH_SIZE;
      }
      return excelRows;
    }
  }

  private ProductReadRow readRow(Row row) {
    ProductReadRow productRow = new ProductReadRow();
    productRow.setName(row.getCell(0).getStringCellValue());
    productRow.setPrice(BigDecimal.valueOf(row.getCell(1).getNumericCellValue()));
    productRow.setQuantity((int) row.getCell(2).getNumericCellValue());
    productRow.setStatus(ProductStatus.valueOf(row.getCell(3).getStringCellValue()));
    productRow.setAvailableFrom(DateUtil.parse(row.getCell(4).getStringCellValue(),
        CommonConstant.DATE_TIME_FORMAT));
    productRow.setIsActive(row.getCell(5).getBooleanCellValue());
    productRow.setBrandId((int) row.getCell(6).getNumericCellValue());
    productRow.setCategoryIds(row.getCell(7).getStringCellValue());
    return productRow;
  }
}
