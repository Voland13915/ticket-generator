package tickets.io;

import org.junit.jupiter.api.Test;
import tickets.model.ResultRecord;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Контракт ResultsStore: любая реализация обязана вести себя так, иначе TicketService сломается.
 * Подклассы лишь создают конкретное хранилище, поэтому реализации взаимозаменяемы (принцип Лисков).
 */
public abstract class ResultsStoreContractTest {
    private static final LocalDateTime T = LocalDateTime.of(2026, 10, 1, 9, 0, 0);

    protected abstract ResultsStore createStore() throws IOException;

    private static ResultRecord record(String last, int ticket, int minute, boolean repeat) {
        return new ResultRecord("TI-237", last, "Иван", ticket, T.plusMinutes(minute), repeat);
    }

    @Test
    void newStoreIsEmpty() throws IOException {
        assertTrue(createStore().readAll().isEmpty());
    }

    @Test
    void appendedRecordIsReadBackUnchanged() throws IOException {
        ResultsStore store = createStore();
        ResultRecord written = record("Иванов", 7, 0, false);

        store.append(written);

        assertEquals(List.of(written), store.readAll());
    }

    @Test
    void recordsComeBackInAppendOrder() throws IOException {
        ResultsStore store = createStore();
        List<ResultRecord> written = List.of(
                record("Иванов", 7, 0, false), record("Петров", 3, 1, false), record("Иванов", 7, 2, true));
        for (ResultRecord r : written) {
            store.append(r);
        }

        assertEquals(written, store.readAll());
    }

    @Test
    void laterAppendsDoNotChangeEarlierRecords() throws IOException {
        ResultsStore store = createStore();
        ResultRecord first = record("Иванов", 7, 0, false);
        store.append(first);
        store.append(record("Петров", 3, 1, false));

        assertEquals(first, store.readAll().get(0));
    }

    @Test
    void repeatRowsForSameStudentAreKept() throws IOException {
        ResultsStore store = createStore();

        store.append(record("Иванов", 7, 0, false));
        store.append(record("Иванов", 7, 5, true));

        assertEquals(List.of(false, true), store.readAll().stream().map(ResultRecord::repeat).toList());
    }
}
