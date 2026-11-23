package tickets.io;

import org.apache.poi.ss.usermodel.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/** Перевод значений Java в ячейки Excel и обратно. */
final class ExcelCells {
    private static final String DATE_FORMAT = "dd.mm.yyyy hh:mm:ss";

    private ExcelCells() {
    }

    /** Значения: String, Number или LocalDateTime (сохраняется как настоящая дата Excel). */
    static void write(Cell cell, Object value) {
        if (value instanceof LocalDateTime date) {
            cell.setCellValue(Date.from(date.atZone(ZoneId.systemDefault()).toInstant()));
            cell.setCellStyle(dateStyle(cell.getSheet().getWorkbook()));
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    /** String, Double, LocalDateTime, Boolean или null для пустой ячейки. */
    static Object read(Cell cell) {
        if (cell == null) {
            return null;
        }
        CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
        return switch (type) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? LocalDateTime.ofInstant(cell.getDateCellValue().toInstant(), ZoneId.systemDefault())
                    : (Object) cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            default -> null;
        };
    }

    /** Ищет уже существующий стиль даты, чтобы не плодить новый стиль на каждую строку. */
    private static CellStyle dateStyle(Workbook wb) {
        for (int i = 0; i < wb.getNumCellStyles(); i++) {
            CellStyle style = wb.getCellStyleAt(i);
            if (DATE_FORMAT.equals(style.getDataFormatString())) {
                return style;
            }
        }
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat(DATE_FORMAT));
        return style;
    }
}
