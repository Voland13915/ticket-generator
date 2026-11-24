package tickets.ui;

import org.junit.jupiter.api.Test;
import tickets.model.ResultRecord;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryTableModelTest {
    private static final ResultRecord FIRST =
            new ResultRecord("TI-237", "Иванов", "Иван", 7, LocalDateTime.of(2026, 10, 30, 9, 12, 5), false);
    private static final ResultRecord REPEAT =
            new ResultRecord("TI-237", "Иванов", "Иван", 7, LocalDateTime.of(2026, 10, 30, 9, 23, 5), true);

    @Test
    void rowShowsAllColumnsInJournalOrder() {
        HistoryTableModel model = new HistoryTableModel();
        model.setRecords(List.of(FIRST));

        assertEquals(1, model.getRowCount());
        assertEquals(6, model.getColumnCount());
        assertEquals(List.of("TI-237", "Иванов", "Иван", "7", "30.10.2026 09:12:05", "нет"),
                List.of(model.getValueAt(0, 0), model.getValueAt(0, 1), model.getValueAt(0, 2),
                        model.getValueAt(0, 3), model.getValueAt(0, 4), model.getValueAt(0, 5)));
    }

    @Test
    void repeatIsShownAsYes() {
        HistoryTableModel model = new HistoryTableModel();
        model.setRecords(List.of(FIRST, REPEAT));

        assertEquals("да", model.getValueAt(1, HistoryTableModel.REPEAT_COLUMN));
    }

    @Test
    void addedRecordGoesToTheEnd() {
        HistoryTableModel model = new HistoryTableModel();
        model.setRecords(List.of(FIRST));

        model.add(REPEAT);

        assertEquals(2, model.getRowCount());
        assertSame(REPEAT, model.recordAt(1));
    }

    @Test
    void rowWithoutDateHasEmptyDateCell() {
        HistoryTableModel model = new HistoryTableModel();
        model.setRecords(List.of(new ResultRecord("TI-237", "Иванов", "Иван", 7, null, false)));

        assertEquals("", model.getValueAt(0, HistoryTableModel.DATE_COLUMN));
    }

    @Test
    void recordsCannotBeChangedFromOutside() {
        HistoryTableModel model = new HistoryTableModel();
        model.setRecords(List.of(FIRST));

        assertThrows(UnsupportedOperationException.class, () -> model.records().add(REPEAT));
    }
}
