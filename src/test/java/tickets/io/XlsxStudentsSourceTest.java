package tickets.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tickets.TestFiles;
import tickets.model.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class XlsxStudentsSourceTest {

    @TempDir
    Path dir;

    @Test
    void sheetIsGroupAndRowsAreStudents() throws IOException {
        Map<String, List<List<String>>> sheets = new LinkedHashMap<>();
        sheets.put("TI-237", List.of(
                List.of("Фамилия", "Имя"),
                List.of("  Иванов ", "Иван"),
                List.of("", ""),
                List.of("Петров", "Пётр")));
        sheets.put("TI-238", List.of(List.of("Фамилия", "Имя"), List.of("Сидоров", "Сидор")));
        Path file = TestFiles.workbook(dir.resolve("students.xlsx"), sheets);

        Map<String, List<Student>> groups = new XlsxStudentsSource(file).read();

        assertEquals(List.of("TI-237", "TI-238"), List.copyOf(groups.keySet()));
        assertEquals(List.of(new Student("TI-237", "Иванов", "Иван"), new Student("TI-237", "Петров", "Пётр")),
                groups.get("TI-237"));
        assertEquals(List.of(new Student("TI-238", "Сидоров", "Сидор")), groups.get("TI-238"));
    }

    @Test
    void headerRowIsNotAStudent() throws IOException {
        Path file = TestFiles.workbook(dir.resolve("students.xlsx"),
                TestFiles.sheets("TI-237", List.of(List.of("Фамилия", "Имя"), List.of("Иванов", "Иван"))));

        assertEquals(1, new XlsxStudentsSource(file).read().get("TI-237").size());
    }

    @Test
    void groupWithoutStudentsHasEmptyList() throws IOException {
        Path file = TestFiles.workbook(dir.resolve("students.xlsx"),
                TestFiles.sheets("Пустая", List.of(List.of("Фамилия", "Имя"))));

        assertEquals(Map.of("Пустая", List.of()), new XlsxStudentsSource(file).read());
    }

    @Test
    void rowWithoutFirstNameIsSkipped() throws IOException {
        Path file = TestFiles.workbook(dir.resolve("students.xlsx"),
                TestFiles.sheets("TI-237", List.of(List.of("Фамилия", "Имя"), List.of("Иванов"), List.of("Петров", "Пётр"))));

        assertEquals(List.of("Петров"),
                new XlsxStudentsSource(file).read().get("TI-237").stream().map(Student::lastName).toList());
    }

    @Test
    void reportsBrokenFileAsIOException() throws IOException {
        Path file = Files.writeString(dir.resolve("students.xlsx"), "не excel");

        assertThrows(IOException.class, () -> new XlsxStudentsSource(file).read());
    }

    @Test
    void missingFileIsReportedAsNoSuchFile() {
        assertThrows(NoSuchFileException.class, () -> new XlsxStudentsSource(dir.resolve("none.xlsx")).read());
    }

    @Test
    void nameIsFileName() {
        assertEquals("students.xlsx", new XlsxStudentsSource(dir.resolve("students.xlsx")).name());
    }
}
