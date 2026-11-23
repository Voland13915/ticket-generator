package tickets;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Помощники тестов: создание и чтение .xlsx/.docx во временной папке. */
public final class TestFiles {
    private static final DataFormatter FORMATTER = new DataFormatter();

    private TestFiles() {
    }

    /** Листы по порядку: имя листа → строки (ячейки-строки). */
    public static Map<String, List<List<String>>> sheets(String name, List<List<String>> rows) {
        Map<String, List<List<String>>> sheets = new LinkedHashMap<>();
        sheets.put(name, rows);
        return sheets;
    }

    public static Path workbook(Path file, Map<String, List<List<String>>> sheets) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); OutputStream out = Files.newOutputStream(file)) {
            sheets.forEach((name, rows) -> {
                Sheet sheet = wb.createSheet(name);
                for (int r = 0; r < rows.size(); r++) {
                    Row row = sheet.createRow(r);
                    for (int c = 0; c < rows.get(r).size(); c++) {
                        row.createCell(c).setCellValue(rows.get(r).get(c));
                    }
                }
            });
            wb.write(out);
        }
        return file;
    }

    /** Простой документ: каждая строка — отдельный абзац без автонумерации. */
    public static Path docx(Path file, List<String> paragraphs) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(); OutputStream out = Files.newOutputStream(file)) {
            paragraphs.forEach(text -> doc.createParagraph().createRun().setText(text));
            doc.write(out);
        }
        return file;
    }

    /** Первый лист файла как таблица строк в том виде, в каком их показывает Excel. */
    public static List<List<String>> readSheet(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file); Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            List<List<String>> rows = new ArrayList<>();
            for (Row row : sheet) {
                List<String> cells = new ArrayList<>();
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    cells.add(FORMATTER.formatCellValue(row.getCell(c)));
                }
                rows.add(cells);
            }
            return rows;
        }
    }
}
