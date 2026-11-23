package tickets.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tickets.TestFiles;
import tickets.model.ResultRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultsJournalTest {
    private static final LocalDateTime T1 = LocalDateTime.of(2026, 10, 1, 9, 0, 0);

    @TempDir
    Path dir;

    private static ResultRecord record(String last, int ticket, LocalDateTime at, boolean repeat) {
        return new ResultRecord("TI-237", last, "Иван", ticket, at, repeat);
    }

    @Test
    void createsFileWithAssignmentHeader() throws IOException {
        Path file = dir.resolve("results.xlsx");

        new ResultsJournal(file).append(record("Иванов", 7, T1, false));

        var sheet = TestFiles.readSheet(file);
        assertEquals(List.of("Группа", "Фамилия", "Имя", "Номер билета", "Дата и время", "Повтор (да/нет)"), sheet.get(0));
        assertEquals(List.of("TI-237", "Иванов", "Иван", "7", "01.10.2026 09:00:00", "нет"), sheet.get(1));
    }

    @Test
    void repeatIsWrittenAsYes() throws IOException {
        Path file = dir.resolve("results.xlsx");
        ResultsJournal journal = new ResultsJournal(file);

        journal.append(record("Иванов", 7, T1, false));
        journal.append(record("Иванов", 7, T1.plusMinutes(5), true));

        var sheet = TestFiles.readSheet(file);
        assertEquals("нет", sheet.get(1).get(5));
        assertEquals("да", sheet.get(2).get(5));
    }

    @Test
    void newRowsGoToTheEndAndOldRowsStayUntouched() throws IOException {
        Path file = dir.resolve("results.xlsx");
        ResultsJournal journal = new ResultsJournal(file);
        journal.append(record("Иванов", 7, T1, false));
        journal.append(record("Петров", 3, T1.plusMinutes(1), false));
        var before = TestFiles.readSheet(file);

        journal.append(record("Иванов", 7, T1.plusMinutes(2), true));

        var after = TestFiles.readSheet(file);
        assertEquals(4, after.size());
        assertEquals(before, after.subList(0, 3));
    }

    @Test
    void readAllRestoresFullHistoryInChronologicalOrder() throws IOException {
        ResultsJournal journal = new ResultsJournal(dir.resolve("results.xlsx"));
        List<ResultRecord> written = List.of(
                record("Иванов", 7, T1, false),
                record("Петров", 3, T1.plusMinutes(1), false),
                record("Иванов", 7, T1.plusMinutes(2), true));
        for (ResultRecord r : written) {
            journal.append(r);
        }

        assertEquals(written, new ResultsJournal(dir.resolve("results.xlsx")).readAll());
    }

    @Test
    void readAllOfMissingFileIsEmpty() throws IOException {
        assertTrue(new ResultsJournal(dir.resolve("results.xlsx")).readAll().isEmpty());
    }

    @Test
    void readAllSkipsRowsWithoutStudentOrTicketNumber() throws IOException {
        Path file = TestFiles.workbook(dir.resolve("results.xlsx"), TestFiles.sheets("Журнал", List.of(
                ResultsJournal.HEADER,
                List.of("TI-237", "Иванов", "Иван", "не число", "", "нет"),
                List.of("", "", "", "", "", ""))));

        assertTrue(new ResultsJournal(file).readAll().isEmpty());
    }
}
