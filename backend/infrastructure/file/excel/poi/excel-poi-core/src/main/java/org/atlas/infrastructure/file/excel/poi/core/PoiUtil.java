package org.atlas.infrastructure.file.excel.poi.core;

import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

@UtilityClass
public class PoiUtil {

  public static boolean isNotEmptyRow(Row row) {
    for (Cell cell : row) {
      if (cell.getCellType() != CellType.BLANK) {
        return true;
      }
    }
    return false;
  }
}
