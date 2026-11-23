package tickets.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtomicFileTest {

    @TempDir
    Path dir;

    private static void text(java.io.OutputStream out, String content) throws IOException {
        out.write(content.getBytes(StandardCharsets.UTF_8));
    }

    private List<String> fileNames() throws IOException {
        try (var files = Files.list(dir)) {
            return files.map(p -> p.getFileName().toString()).sorted().toList();
        }
    }

    @Test
    void createsMissingFile() throws IOException {
        Path file = dir.resolve("data.txt");

        AtomicFile.write(file, out -> text(out, "привет"));

        assertEquals("привет", Files.readString(file));
    }

    @Test
    void replacesExistingContentAndLeavesNoTemporaryFile() throws IOException {
        Path file = Files.writeString(dir.resolve("data.txt"), "старое");

        AtomicFile.write(file, out -> text(out, "новое"));

        assertEquals("новое", Files.readString(file));
        assertEquals(List.of("data.txt"), fileNames());
    }

    @Test
    void failedWriteKeepsOldFileIntact() throws IOException {
        Path file = Files.writeString(dir.resolve("data.txt"), "старое");

        assertThrows(IOException.class, () -> AtomicFile.write(file, out -> {
            text(out, "полови");
            throw new IOException("диск полон");
        }));

        assertEquals("старое", Files.readString(file));
        assertEquals(List.of("data.txt"), fileNames());
    }

    @Test
    void reportsFileThatCannotBeReplacedAsLocked() throws IOException {
        // непустая папка на месте файла: подмена невозможна, как и у файла, занятого Excel
        Path target = Files.createDirectory(dir.resolve("data.txt"));
        Files.writeString(target.resolve("inside.txt"), "x");

        assertThrows(FileLockedException.class, () -> AtomicFile.write(target, out -> text(out, "новое")));

        assertEquals(List.of("data.txt"), fileNames());
    }
}
