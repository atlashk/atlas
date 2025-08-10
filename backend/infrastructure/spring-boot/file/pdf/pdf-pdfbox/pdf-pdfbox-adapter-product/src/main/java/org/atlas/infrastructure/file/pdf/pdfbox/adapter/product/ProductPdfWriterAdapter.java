package org.atlas.infrastructure.file.pdf.pdfbox.adapter.product;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.atlas.domain.product.port.file.model.write.ProductRow;
import org.atlas.domain.product.port.file.pdf.ProductPdfWriterPort;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.util.DateUtil;
import org.springframework.stereotype.Component;

@Component
public class ProductPdfWriterAdapter implements ProductPdfWriterPort {

  @Override
  public byte[] write(List<ProductRow> productRows) throws IOException {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage();
      document.addPage(page);
      PDPageContentStream contentStream = new PDPageContentStream(document, page);

      // Fonts
      PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
      PDType1Font dataFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

      // Title on first page
      contentStream.setFont(headerFont, 12);
      contentStream.beginText();
      contentStream.newLineAtOffset(25, 750);
      contentStream.showText("Product List");
      contentStream.endText();

      // Table setup
      float margin = 25;
      float topY = 720;
      float cellHeight = 20;
      float fontSize = 8;
      float textPaddingY = 13; // Padding from top of cell for text baseline
      float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
      int numCols = 9;
      float[] cellWidths = {30, 100, 50, 50, 50, 80, 40, 50, 60}; // Custom widths for better fit
      float totalCellWidth = 0;
      for (float w : cellWidths) {
        totalCellWidth += w;
      }
      if (totalCellWidth > tableWidth) {
        float scale = tableWidth / totalCellWidth;
        for (int i = 0; i < cellWidths.length; i++) {
          cellWidths[i] *= scale; // Adjust to fit page
        }
        totalCellWidth = tableWidth; // Update totalCellWidth to reflect the scaled sum
      }

      String[] headers = new String[]{
          "ID", "Name", "Price", "Quantity", "Status", "Available From", "Active", "Brand ID",
          "Category IDs"
      };

      int rowIndex = 0;
      float bottomMargin = 25;
      int maxRowsPerPage =
          (int) ((topY - bottomMargin) / cellHeight) - 1; // Subtract 1 for header row

      while (rowIndex < productRows.size()) {
        float y = topY;

        // Draw headers
        contentStream.setFont(headerFont, fontSize);
        float xOffset = margin;
        for (int j = 0; j < numCols; j++) {
          contentStream.beginText();
          contentStream.newLineAtOffset(xOffset + 2, y - textPaddingY);
          contentStream.showText(truncateText(headers[j], cellWidths[j], fontSize, headerFont));
          contentStream.endText();
          xOffset += cellWidths[j];
        }

        // Draw data rows on this page
        contentStream.setFont(dataFont, fontSize);
        int rowsOnPage = 0;
        for (; rowIndex < productRows.size() && rowsOnPage < maxRowsPerPage;
            rowIndex++, rowsOnPage++) {
          ProductRow row = productRows.get(rowIndex);
          String[] data = new String[]{
              row.getId() != null ? String.valueOf(row.getId()) : "",
              row.getName() != null ? row.getName() : "",
              row.getPrice() != null ? String.format("%.2f", row.getPrice()) : "",
              row.getQuantity() != null ? String.valueOf(row.getQuantity()) : "",
              row.getStatus() != null ? row.getStatus().name() : "",
              row.getAvailableFrom() != null ? DateUtil.format(row.getAvailableFrom(),
                  CommonConstant.DATE_TIME_FORMAT) : "",
              String.valueOf(row.getIsActive()),
              row.getBrandId() != null ? String.valueOf(row.getBrandId()) : "",
              row.getCategoryIds() != null ? row.getCategoryIds() : ""
          };

          xOffset = margin;
          for (int j = 0; j < numCols; j++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(xOffset + 2,
                y - (rowsOnPage + 1) * cellHeight - textPaddingY);
            contentStream.showText(truncateText(data[j], cellWidths[j], fontSize, dataFont));
            contentStream.endText();
            xOffset += cellWidths[j];
          }
        }

        // Draw grid lines for this page
        int totalRowsOnPage = 1 + rowsOnPage; // Header + data rows
        contentStream.setLineWidth(1f);
        for (int i = 0; i <= totalRowsOnPage; i++) {
          contentStream.moveTo(margin, y - i * cellHeight);
          contentStream.lineTo(margin + totalCellWidth, y - i * cellHeight);
          contentStream.stroke();
        }
        xOffset = margin;
        for (int j = 0; j <= numCols; j++) {
          contentStream.moveTo(xOffset, y);
          contentStream.lineTo(xOffset, y - totalRowsOnPage * cellHeight);
          contentStream.stroke();
          if (j < numCols) {
            xOffset += cellWidths[j];
          }
        }

        // Add new page if needed
        if (rowIndex < productRows.size()) {
          contentStream.close();
          page = new PDPage();
          document.addPage(page);
          contentStream = new PDPageContentStream(document, page);
        }
      }

      contentStream.close();

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      document.save(byteArrayOutputStream);
      return byteArrayOutputStream.toByteArray();
    }
  }

  // Helper method to truncate text to fit within cell width
  private String truncateText(String text, float cellWidth, float fontSize, PDType1Font font)
      throws IOException {
    if (text == null) {
      return "";
    }
    float stringWidth = font.getStringWidth(text) / 1000 * fontSize;
    if (stringWidth <= cellWidth - 4) {
      return text; // 4 for padding
    }
    int endIndex = (int) (text.length() * (cellWidth - 4) / stringWidth);
    if (endIndex < 3) {
      return "";
    }
    return text.substring(0, Math.max(3, endIndex - 3)) + "...";
  }
}
