package tickets.console;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tickets.TestFiles;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Журнал консольного режима (Лаб 1): шапка, дозапись, старые строки не меняются. */
class JournalTest {
    private static final LocalDateTime AT = LocalDateTime.of(2026, 9, 30, 11, 24, 38);

    @TempDir
    Path dir;

    @Test
    void writesHeaderAndAppendsAcrossRestarts() throws IOException {
        Path file = dir.resolve("journal.xlsx");
        new Journal(file).append(new Entry("Migov", "Daniil", 7, AT));
        new Journal(file).append(new Entry("Poddubnii", "Roman", 8, AT.plusMinutes(1)));

        var sheet = TestFiles.readSheet(file);

        assertEquals(List.of("Last name", "First name", "Номер билета", "Дата и время"), sheet.get(0));
        assertEquals(List.of("Migov", "Daniil", "7", "30.09.2026 11:24:38"), sheet.get(1));
        assertEquals(List.of("Poddubnii", "Roman", "8", "30.09.2026 11:25:38"), sheet.get(2));
        assertEquals(3, sheet.size());
    }
}
