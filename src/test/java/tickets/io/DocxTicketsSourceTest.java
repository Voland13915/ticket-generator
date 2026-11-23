package tickets.io;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tickets.TestFiles;
import tickets.model.Ticket;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocxTicketsSourceTest {

    @TempDir
    Path dir;

    @Test
    void readsTicketsFromRealDocxFile() throws IOException {
        Path file = TestFiles.docx(dir.resolve("tickets.docx"), List.of(
                "Билет 1", "1. Вопрос первый", "2. Вопрос второй", "3. Вопрос третий", "",
                "Билет 2", "1. Единственный вопрос"));

        var result = new DocxTicketsSource(file).read();

        assertEquals(List.of(new Ticket(1, List.of("Вопрос первый", "Вопрос второй", "Вопрос третий"))),
                result.tickets());
        assertEquals(1, result.warnings().size());
    }

    @Test
    void detectsWordNumberedListParagraphs() throws IOException {
        Path file = dir.resolve("numbered.docx");
        try (XWPFDocument doc = new XWPFDocument(); OutputStream out = Files.newOutputStream(file)) {
            doc.createParagraph().createRun().setText("Билет 9");
            for (String question : List.of("Первый", "Второй", "Третий")) {
                XWPFParagraph paragraph = doc.createParagraph();
                paragraph.setNumID(BigInteger.ONE);
                paragraph.createRun().setText(question);
            }
            doc.write(out);
        }

        var result = new DocxTicketsSource(file).read();

        assertEquals(List.of("Первый", "Второй", "Третий"), result.tickets().get(0).questions());
    }

    @Test
    void reportsBrokenFileAsIOException() throws IOException {
        Path file = Files.writeString(dir.resolve("broken.docx"), "это не docx");

        IOException e = assertThrows(IOException.class, () -> new DocxTicketsSource(file).read());
        assertTrue(e.getMessage().contains("broken.docx"));
    }

    @Test
    void missingFileIsReportedAsNoSuchFile() {
        assertThrows(NoSuchFileException.class, () -> new DocxTicketsSource(dir.resolve("none.docx")).read());
    }
}
