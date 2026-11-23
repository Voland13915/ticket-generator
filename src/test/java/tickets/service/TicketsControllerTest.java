package tickets.service;

import org.junit.jupiter.api.Test;
import tickets.io.InMemoryResultsStore;
import tickets.model.Assignment;
import tickets.model.ResultRecord;
import tickets.model.Student;
import tickets.model.Ticket;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class TicketsControllerTest {
    private static final Student IVANOV = new Student("TI-237", "Иванов", "Иван");
    private static final Catalog CATALOG = new Catalog(
            Map.of("TI-237", List.of(IVANOV)),
            IntStream.rangeClosed(1, 5).mapToObj(n -> new Ticket(n, List.of("a", "b", "c"))).toList());
    private static final LoadResult LOADED = new LoadResult(CATALOG, List.of(), List.of("билет 6 пропущен"));
    private static final LoadResult FAILED =
            new LoadResult(null, List.of("Не найден файл students.xlsx."), List.of());

    private final InMemoryResultsStore store = new InMemoryResultsStore();

    private TicketsController controller(CatalogSource source) {
        return new TicketsController(source, store, new Random(1),
                Clock.fixed(Instant.parse("2026-10-01T09:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void failedLoadIsNotReadyAndCarriesErrors() {
        var snapshot = controller(() -> FAILED).reload();

        assertFalse(snapshot.ready());
        assertNull(snapshot.catalog());
        assertEquals(List.of("Не найден файл students.xlsx."), snapshot.errors());
    }

    @Test
    void successfulLoadCarriesCatalogWarningsAndHistory() {
        ResultRecord old = new ResultRecord("TI-237", "Иванов", "Иван", 3, LocalDateTime.of(2026, 9, 1, 9, 0), false);
        store.records.add(old);

        var snapshot = controller(() -> LOADED).reload();

        assertTrue(snapshot.ready());
        assertEquals(CATALOG, snapshot.catalog());
        assertEquals(List.of("билет 6 пропущен"), snapshot.warnings());
        assertEquals(List.of(old), snapshot.history());
        assertNull(snapshot.historyError());
    }

    @Test
    void unreadableJournalDoesNotStopTheApplication() {
        store.readFailures = 1;

        var snapshot = controller(() -> LOADED).reload();

        assertTrue(snapshot.ready());
        assertTrue(snapshot.history().isEmpty());
        assertEquals("файл занят", snapshot.historyError());
    }

    @Test
    void generateBeforeSuccessfulLoadIsRejected() {
        TicketsController controller = controller(() -> FAILED);

        assertThrows(IllegalStateException.class, () -> controller.generate(IVANOV, e -> false));
        controller.reload();
        assertThrows(IllegalStateException.class, () -> controller.generate(IVANOV, e -> false));
    }

    @Test
    void worksAfterInputFilesAreFixedAndReloaded() throws Exception {
        List<LoadResult> answers = new ArrayList<>(List.of(FAILED, LOADED));
        TicketsController controller = controller(() -> answers.remove(0));

        assertFalse(controller.reload().ready());
        assertTrue(controller.reload().ready());

        assertTrue(controller.generate(IVANOV, e -> false).isPresent());
    }

    @Test
    void retriesWhileJournalIsLockedAndThenSavesOnce() throws Exception {
        store.appendFailures = 2;
        List<String> asked = new ArrayList<>();
        TicketsController controller = controller(() -> LOADED);
        controller.reload();

        Optional<Assignment> result = controller.generate(IVANOV, e -> asked.add(e.getMessage()));

        assertTrue(result.isPresent());
        assertEquals(List.of("файл занят", "файл занят"), asked);
        assertEquals(1, store.records.size());
    }

    @Test
    void cancelledRetrySavesNothing() throws Exception {
        store.appendFailures = 5;
        TicketsController controller = controller(() -> LOADED);
        controller.reload();

        Optional<Assignment> result = controller.generate(IVANOV, e -> false);

        assertTrue(result.isEmpty());
        assertTrue(store.records.isEmpty());
    }

    @Test
    void repeatedGenerationGivesSameTicket() throws Exception {
        TicketsController controller = controller(() -> LOADED);
        controller.reload();

        Assignment first = controller.generate(IVANOV, e -> false).orElseThrow();
        Assignment second = controller.generate(IVANOV, e -> false).orElseThrow();

        assertEquals(first.ticket(), second.ticket());
        assertTrue(second.record().repeat());
        assertEquals(2, store.records.size());
    }

    @Test
    void ticketMissingFromDocumentIsReportedNotRetried() throws IOException {
        store.records.add(new ResultRecord("TI-237", "Иванов", "Иван", 99, LocalDateTime.of(2026, 9, 1, 9, 0), false));
        TicketsController controller = controller(() -> LOADED);
        controller.reload();

        assertThrows(TicketUnavailableException.class, () -> controller.generate(IVANOV, e -> true));
        assertEquals(1, store.records.size());
    }
}
