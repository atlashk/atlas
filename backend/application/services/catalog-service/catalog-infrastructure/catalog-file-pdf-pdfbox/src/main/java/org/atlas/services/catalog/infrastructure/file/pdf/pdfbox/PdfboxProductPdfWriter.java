package org.atlas.services.catalog.infrastructure.file.pdf.pdfbox;

import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.atlas.libs.file.pdf.pdfbox.PdfboxUtil;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.services.catalog.port.out.file.model.ProductWriteRow;
import org.atlas.services.catalog.port.out.file.pdf.ProductPdfWriter;
import org.springframework.stereotype.Component;

@Component
public class PdfboxProductPdfWriter implements ProductPdfWriter {

  private static final float MARGIN = 25;
  private static final float TOP_Y = 720;
  private static final float CELL_HEIGHT = 20;
  private static final float FONT_SIZE = 8;
  private static final float TEXT_PADDING_Y = 13;
  private static final float BOTTOM_MARGIN = 25;

  private static final String[] HEADERS = {
      "ID", "Name", "Price", "Quantity", "Stock Status",
      "Available From", "Active", "Brand ID", "Category IDs"
  };

  private static final float[] CELL_WIDTHS = {
      30, 100, 50, 50, 50, 80, 40, 50, 60
  };

  @Override
  public byte[] write(List<ProductWriteRow> productRows) throws IOException {
    try (PDDocument document = new PDDocument()) {
      PDPage page = createPage(document);
      PDPageContentStream contentStream = new PDPageContentStream(document, page);

      PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
      PDType1Font dataFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

      writeTitle(contentStream, headerFont, "Product List");

      float[] adjustedCellWidths = adjustCellWidths(page, CELL_WIDTHS);
      int maxRowsPerPage = calculateMaxRowsPerPage();

      int rowIndex = 0;
      while (rowIndex < productRows.size()) {
        float y = TOP_Y;

        drawHeaderRow(contentStream, headerFont, adjustedCellWidths, y);

        int rowsOnPage = 0;
        rowIndex = drawDataRows(contentStream, dataFont, adjustedCellWidths,
            productRows, rowIndex, maxRowsPerPage, rowsOnPage, y);

        drawGrid(contentStream, adjustedCellWidths, y, rowsOnPage);

        if (rowIndex < productRows.size()) {
          contentStream.close();
          page = createPage(document);
          contentStream = new PDPageContentStream(document, page);
        }
      }

      contentStream.close();

      return PdfboxUtil.saveDocumentToBytes(document);
    }
  }

  private PDPage createPage(PDDocument document) {
    PDPage page = new PDPage();
    document.addPage(page);
    return page;
  }

  private void writeTitle(PDPageContentStream contentStream, PDType1Font font, String title)
      throws IOException {
    contentStream.setFont(font, 12);
    contentStream.beginText();
    contentStream.newLineAtOffset(MARGIN, 750);
    contentStream.showText(title);
    contentStream.endText();
  }

  private float[] adjustCellWidths(PDPage page, float[] originalWidths) {
    float tableWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
    float totalCellWidth = 0;
    for (float w : originalWidths) {
      totalCellWidth += w;
    }
    if (totalCellWidth > tableWidth) {
      float scale = tableWidth / totalCellWidth;
      float[] scaled = new float[originalWidths.length];
      for (int i = 0; i < originalWidths.length; i++) {
        scaled[i] = originalWidths[i] * scale;
      }
      return scaled;
    }
    return originalWidths.clone();
  }

  private int calculateMaxRowsPerPage() {
    return (int) ((TOP_Y - BOTTOM_MARGIN) / CELL_HEIGHT) - 1;
  }

  private void drawHeaderRow(PDPageContentStream contentStream, PDType1Font font,
      float[] cellWidths, float y) throws IOException {
    contentStream.setFont(font, FONT_SIZE);
    float xOffset = MARGIN;
    for (int j = 0; j < HEADERS.length; j++) {
      writeCellText(contentStream, font, HEADERS[j], xOffset, y - TEXT_PADDING_Y,
          cellWidths[j]);
      xOffset += cellWidths[j];
    }
  }

  private int drawDataRows(PDPageContentStream contentStream, PDType1Font font,
      float[] cellWidths, List<ProductWriteRow> productRows,
      int startIndex, int maxRowsPerPage, int rowsOnPage, float y) throws IOException {
    contentStream.setFont(font, FONT_SIZE);

    for (; startIndex < productRows.size() && rowsOnPage < maxRowsPerPage;
        startIndex++, rowsOnPage++) {
      ProductWriteRow row = productRows.get(startIndex);
      String[] data = mapRowToData(row);

      float xOffset = MARGIN;
      for (int j = 0; j < data.length; j++) {
        writeCellText(contentStream, font, data[j], xOffset,
            y - (rowsOnPage + 1) * CELL_HEIGHT - TEXT_PADDING_Y,
            cellWidths[j]);
        xOffset += cellWidths[j];
      }
    }
    return startIndex;
  }

  private void drawGrid(PDPageContentStream contentStream, float[] cellWidths,
      float y, int rowsOnPage) throws IOException {
    int totalRowsOnPage = 1 + rowsOnPage;
    contentStream.setLineWidth(1f);

    // Horizontal lines
    for (int i = 0; i <= totalRowsOnPage; i++) {
      contentStream.moveTo(MARGIN, y - i * CELL_HEIGHT);
      contentStream.lineTo(MARGIN + sum(cellWidths), y - i * CELL_HEIGHT);
      contentStream.stroke();
    }

    // Vertical lines
    float xOffset = MARGIN;
    for (float cellWidth : cellWidths) {
      contentStream.moveTo(xOffset, y);
      contentStream.lineTo(xOffset, y - totalRowsOnPage * CELL_HEIGHT);
      contentStream.stroke();
      xOffset += cellWidth;
    }
    // Last vertical line
    contentStream.moveTo(xOffset, y);
    contentStream.lineTo(xOffset, y - totalRowsOnPage * CELL_HEIGHT);
    contentStream.stroke();
  }

  private String[] mapRowToData(ProductWriteRow row) {
    return new String[]{
        row.getId(),
        row.getName(),
        row.getType().name(),
        row.getPrice() != null ? String.format("%.2f", row.getPrice()) : "",
        DateUtil.format(row.getPublishedAt(), CommonConstant.DATE_TIME_FORMAT),
        String.valueOf(row.getInStock()),
        row.getBrandName(),
        row.getCategoryNames()
    };
  }

  private void writeCellText(PDPageContentStream contentStream, PDType1Font font,
      String text, float x, float y, float cellWidth) throws IOException {
    contentStream.beginText();
    contentStream.newLineAtOffset(x + 2, y);
    contentStream.showText(truncateText(text, cellWidth, FONT_SIZE, font));
    contentStream.endText();
  }

  private String truncateText(String text, float cellWidth, float fontSize, PDType1Font font)
      throws IOException {
    if (text == null) {
      return "";
    }
    float stringWidth = font.getStringWidth(text) / 1000 * fontSize;
    if (stringWidth <= cellWidth - 4) {
      return text;
    }
    int endIndex = (int) (text.length() * (cellWidth - 4) / stringWidth);
    if (endIndex < 3) {
      return "";
    }
    return text.substring(0, Math.max(3, endIndex - 3)) + "...";
  }

  private float sum(float[] arr) {
    float total = 0;
    for (float v : arr) {
      total += v;
    }
    return total;
  }
}
