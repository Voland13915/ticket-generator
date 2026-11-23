package tickets.io;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Журнал в .xlsx: шапка в первой строке, дальше строки только дописываются в конец.
 * Общая основа и для консольного журнала (Лаб 1), и для results.xlsx (Лаб 2).
 * Значения ячеек переводит {@link ExcelCells}, безопасную запись выполняет {@link AtomicFile}.
 */
public final class XlsxLog {
    private final Path file;
    private final String sheetName;
    private final List<String> header;

    public XlsxLog(Path file, String sheetName, List<String> header) {
        this.file = file;
        this.sheetName = sheetName;
        this.header = List.copyOf(header);
    }

    /** Дописывает строку и сразу сохраняет файл. Значения: String, Number или LocalDateTime. */
    public void append(List<?> values) throws IOException {
        try (Workbook wb = open()) {
            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            for (int i = 0; i < values.size(); i++) {
                ExcelCells.write(row.createCell(i), values.get(i));
            }
            AtomicFile.write(file, wb::write);
        }
    }

    /** Строки данных без шапки; значения как в {@link ExcelCells#read}. */
    public List<List<Object>> readRows() throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }
        try (Workbook wb = ExcelFiles.open(file)) {
            Sheet sheet = wb.getSheetAt(0);
            List<List<Object>> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                List<Object> values = new ArrayList<>();
                for (int c = 0; c < header.size(); c++) {
                    values.add(ExcelCells.read(row.getCell(c)));
                }
                rows.add(values);
            }
            return rows;
        }
    }

    /** Открывает существующий файл или создаёт новую книгу с шапкой. */
    private Workbook open() throws IOException {
        Workbook wb = Files.exists(file) ? ExcelFiles.open(file) : new XSSFWorkbook();
        Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : wb.createSheet(sheetName);
        if (sheet.getPhysicalNumberOfRows() == 0) {
            writeHeader(wb, sheet);
        }
        return wb;
    }

    private void writeHeader(Workbook wb, Sheet sheet) {
        Font bold = wb.createFont();
        bold.setBold(true);
        CellStyle style = wb.createCellStyle();
        style.setFont(bold);

        Row row = sheet.createRow(0);
        for (int i = 0; i < header.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(header.get(i));
            cell.setCellStyle(style);
            sheet.setColumnWidth(i, 22 * 256);
        }
    }
}
