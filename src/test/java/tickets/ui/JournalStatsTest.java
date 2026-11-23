package tickets.ui;

import org.junit.jupiter.api.Test;
import tickets.model.ResultRecord;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JournalStatsTest {
    private static final LocalDateTime T = LocalDateTime.of(2026, 10, 30, 9, 0);

    private static ResultRecord record(String group, String last, boolean repeat) {
        return new ResultRecord(group, last, "Иван", 7, T, repeat);
    }

    @Test
    void emptyJournal() {
        JournalStats stats = JournalStats.of(List.of());

        assertEquals(new JournalStats(0, 0, 0), stats);
        assertEquals("Записей пока нет", stats.describe());
    }

    @Test
    void countsIssuesDistinctStudentsAndRepeats() {
        JournalStats stats = JournalStats.of(List.of(
                record("TI-237", "Иванов", false),
                record("TI-237", "Петров", false),
                record("TI-237", " иванов ", true),   // тот же студент: регистр и пробелы не важны
                record("TI-238", "Иванов", false)));  // однофамилец из другой группы — другой студент

        assertEquals(new JournalStats(4, 3, 1), stats);
        assertEquals("4 выдачи, 3 студента, 1 повтор", stats.describe());
    }

    @Test
    void manyRepeatsUseGenitivePlural() {
        List<ResultRecord> records = List.of(
                record("TI-237", "Иванов", false), record("TI-237", "Иванов", true),
                record("TI-237", "Иванов", true), record("TI-237", "Иванов", true),
                record("TI-237", "Иванов", true), record("TI-237", "Иванов", true));

        assertEquals("6 выдач, 1 студент, 5 повторов", JournalStats.of(records).describe());
    }
}
