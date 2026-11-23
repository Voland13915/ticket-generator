package tickets.io;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** tickets.docx: достаёт абзацы и передаёт их в {@link TicketsParser}. */
public final class DocxTicketsSource implements TicketsSource {
    private final Path file;

    public DocxTicketsSource(Path file) {
        this.file = file;
    }

    @Override
    public String name() {
        return file.getFileName().toString();
    }

    @Override
    public TicketsParser.Result read() throws IOException {
        try (InputStream in = Files.newInputStream(file); XWPFDocument doc = new XWPFDocument(in)) {
            List<TicketsParser.Paragraph> paragraphs = doc.getParagraphs().stream()
                    .map(p -> new TicketsParser.Paragraph(p.getText(), isNumbered(p)))
                    .toList();
            return TicketsParser.parse(paragraphs);
        } catch (RuntimeException e) {
            throw new IOException("Файл " + file.getFileName() + " повреждён или не является документом .docx", e);
        }
    }

    private static boolean isNumbered(XWPFParagraph paragraph) {
        return paragraph.getNumID() != null;
    }
}
