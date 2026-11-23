package tickets.io;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Открытие .xlsx: любые ошибки разбора превращаются в IOException с понятным текстом. */
final class ExcelFiles {

    private ExcelFiles() {
    }

    /** Книга целиком читается в память, файл после вызова не остаётся открытым. */
    static Workbook open(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return new XSSFWorkbook(in);
        } catch (RuntimeException e) {
            throw new IOException("Файл " + file.getFileName() + " повреждён или не является книгой .xlsx", e);
        }
    }
}
