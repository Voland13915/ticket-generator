package tickets.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tickets.TestFiles;
import tickets.io.DocxTicketsSource;
import tickets.io.XlsxStudentsSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Загрузчик вместе с настоящими Excel- и Word-источниками. */
class SourceLoaderFilesTest {
    private static final List<String> DOC = List.of("Билет 1", "1. a", "2. b", "3. c", "Билет 2", "1. a", "2. b");

    @TempDir
    Path dir;

    private SourceLoader loader() {
        return new SourceLoader(new XlsxStudentsSource(dir.resolve("students.xlsx")),
                new DocxTicketsSource(dir.resolve("tickets.docx")));
    }

    private void writeStudents() throws IOException {
        TestFiles.workbook(dir.resolve("students.xlsx"), TestFiles.sheets("TI-237",
                List.of(List.of("Фамилия", "Имя"), List.of("Иванов", "Иван"))));
    }

    @Test
    void loadsRealFiles() throws IOException {
        writeStudents();
        TestFiles.docx(dir.resolve("tickets.docx"), DOC);

        LoadResult result = loader().load();

        assertTrue(result.ok());
        assertEquals(1, result.catalog().groups().size());
        assertEquals(1, result.catalog().tickets().size());
        assertEquals(1, result.warnings().size()); // билет 2 без третьего вопроса
    }

    @Test
    void missingFilesNameTheFolderToPutThemIn() {
        LoadResult result = loader().load();

        assertEquals(2, result.errors().size());
        assertTrue(result.errors().get(0).contains("students.xlsx"));
        assertTrue(result.errors().get(1).contains("tickets.docx"));
        assertTrue(result.errors().get(0).contains(dir.toString()));
    }

    @Test
    void brokenFilesAreReportedNotThrown() throws IOException {
        Files.writeString(dir.resolve("students.xlsx"), "мусор");
        Files.writeString(dir.resolve("tickets.docx"), "мусор");

        assertEquals(2, loader().load().errors().size());
    }
}
