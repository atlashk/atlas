package org.atlas.services.catalog.infrastructure.file.excel.poi;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.services.catalog.port.out.file.excel.ProductExcelWriter;
import org.atlas.services.catalog.port.out.file.model.ProductWriteRow;
import org.springframework.stereotype.Component;

@Component
public class PoiProductExcelWriter implements ProductExcelWriter {

  @Override
  public byte[] write(List<ProductWriteRow> productRows) throws Exception {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      XSSFSheet sheet = workbook.createSheet(SHEET_NAME);
      createHeader(workbook, sheet);
      createRows(workbook, sheet, productRows);
      sheet.autoSizeColumn(1);
      workbook.write(outputStream);
      return outputStream.toByteArray();
    }
  }

  private void createHeader(XSSFWorkbook workbook, XSSFSheet sheet) {
    Row header = sheet.createRow(0);

    CellStyle headerStyle = workbook.createCellStyle();
    XSSFFont font = workbook.createFont();
    font.setFontName("Calibri");
    font.setFontHeightInPoints((short) 12);
    font.setBold(true);
    headerStyle.setFont(font);

    Cell headerCell = header.createCell(0);
    headerCell.setCellValue("ID");
    headerCell.setCellStyle(headerStyle);

    headerCell = header.createCell(1);
    headerCell.setCellValue("Name");
    headerCell.setCellStyle(headerStyle);

    headerCell = header.createCell(2);
    headerCell.setCellValue("Type");
    headerCell.setCellStyle(headerStyle);

    headerCell = header.createCell(3);
    headerCell.setCellValue("Price");
    headerCell.setCellStyle(headerStyle);


    headerCell = header.createCell(4);
    headerCell.setCellValue("Quantity");
    headerCell.setCellStyle(headerStyle);

    headerCell = header.createCell(5);
    headerCell.setCellValue("Available From");
    headerCell.setCellStyle(headerStyle);

    headerCell = header.createCell(6);
    headerCell.setCellValue("Active");
    headerCell.setCellStyle(headerStyle);

    headerCell = header.createCell(7);
    headerCell.setCellValue("Brand ID");
    headerCell.setCellStyle(headerStyle);

    headerCell = header.createCell(8);
    headerCell.setCellValue("Category IDs");
    headerCell.setCellStyle(headerStyle);
  }

  private void createRows(XSSFWorkbook workbook, XSSFSheet sheet,
      List<ProductWriteRow> productRows) {
    int rowIndex = 1;
    for (ProductWriteRow productRow : productRows) {
      CellStyle style = workbook.createCellStyle();
      style.setAlignment(HorizontalAlignment.LEFT);
      style.setWrapText(true);

      Row row = sheet.createRow(rowIndex);
      Cell cell = row.createCell(0);
      cell.setCellValue(productRow.getId());
      cell.setCellStyle(style);

      cell = row.createCell(1);
      cell.setCellValue(productRow.getName());
      cell.setCellStyle(style);

      cell = row.createCell(2);
      cell.setCellValue(productRow.getType().name());
      cell.setCellStyle(style);

      cell = row.createCell(3);
      cell.setCellValue(productRow.getPrice().doubleValue());
      cell.setCellStyle(style);

      cell = row.createCell(4);
      cell.setCellValue(DateUtil.format(productRow.getPublishedAt(), CommonConstant.DATE_TIME_FORMAT));
      cell.setCellStyle(style);

      cell = row.createCell(5);
      cell.setCellValue(productRow.getInStock());
      cell.setCellStyle(style);

      cell = row.createCell(6);
      cell.setCellValue(productRow.getBrandName());
      cell.setCellStyle(style);

      cell = row.createCell(7);
      cell.setCellValue(productRow.getCategoryNames());
      cell.setCellStyle(style);

      rowIndex++;
    }
  }
}
