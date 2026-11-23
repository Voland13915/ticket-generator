package tickets.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tickets.TestFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XlsxLogTest {
    private static final List<String> HEADER = List.of("A", "B", "C");

    @TempDir
    Path dir;

    @Test
    void createsFileWithHeaderOnFirstAppend() throws IOException {
        Path file = dir.resolve("log.xlsx");

        new XlsxLog(file, "Журнал", HEADER).append(List.of("x", "y", 3));

        assertEquals(List.of(List.of("A", "B", "C"), List.of("x", "y", "3")), TestFiles.readSheet(file));
    }

    @Test
    void appendsToEndAndKeepsExistingRows() throws IOException {
        Path file = dir.resolve("log.xlsx");
        XlsxLog log = new XlsxLog(file, "Журнал", HEADER);
        log.append(List.of("1", "a", 1));
        log.append(List.of("2", "b", 2));
        var before = TestFiles.readSheet(file);

        log.append(List.of("3", "c", 3));

        var after = TestFiles.readSheet(file);
        assertEquals(4, after.size());
        assertEquals(before, after.subList(0, 3));
        assertEquals(List.of("3", "c", "3"), after.get(3));
    }

    @Test
    void secondInstanceContinuesAfterExistingRows() throws IOException {
        Path file = dir.resolve("log.xlsx");
        new XlsxLog(file, "Журнал", HEADER).append(List.of("1", "a", 1));

        // «следующий запуск» программы: новый объект над тем же файлом
        new XlsxLog(file, "Журнал", HEADER).append(List.of("2", "b", 2));

        assertEquals(3, TestFiles.readSheet(file).size());
    }

    @Test
    void dateIsStoredAsRealExcelDate() throws IOException {
        Path file = dir.resolve("log.xlsx");
        XlsxLog log = new XlsxLog(file, "Журнал", HEADER);
        LocalDateTime moment = LocalDateTime.of(2026, 9, 30, 11, 24, 38);

        log.append(List.of("x", moment, 1));

        assertEquals("30.09.2026 11:24:38", TestFiles.readSheet(file).get(1).get(1));
        assertEquals(moment, log.readRows().get(0).get(1));
    }

    @Test
    void readRowsOfMissingFileIsEmpty() throws IOException {
        assertTrue(new XlsxLog(dir.resolve("none.xlsx"), "Журнал", HEADER).readRows().isEmpty());
    }

    @Test
    void leavesNoTemporaryFileBehind() throws IOException {
        Path file = dir.resolve("log.xlsx");

        new XlsxLog(file, "Журнал", HEADER).append(List.of("x", "y", 1));

        try (var files = Files.list(dir)) {
            assertEquals(List.of("log.xlsx"), files.map(p -> p.getFileName().toString()).toList());
        }
    }

    @Test
    void doesNotOverwriteCorruptedFile() throws IOException {
        Path file = Files.writeString(dir.resolve("log.xlsx"), "это не excel");

        assertThrows(IOException.class, () -> new XlsxLog(file, "Журнал", HEADER).append(List.of("x", "y", 1)));

        assertEquals("это не excel", Files.readString(file));
    }
}
