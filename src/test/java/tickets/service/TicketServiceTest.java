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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-01T09:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 1, 9, 0, 0);
    private static final Student IVANOV = new Student("TI-237", "Иванов", "Иван");

    private final InMemoryResultsStore store = new InMemoryResultsStore();

    private static List<Ticket> tickets(int... numbers) {
        return IntStream.of(numbers).mapToObj(n -> new Ticket(n, List.of("a", "b", "c"))).toList();
    }

    private static List<Ticket> tickets(int count) {
        return tickets(IntStream.rangeClosed(1, count).toArray());
    }

    private TicketService service(List<Ticket> tickets) {
        return new TicketService(tickets, store, new Random(42), CLOCK);
    }

    @Test
    void newStudentGetsTicketFromRangeAndIsNotRepeat() throws Exception {
        Assignment a = service(tickets(20)).assign(IVANOV);

        assertTrue(a.ticket().number() >= 1 && a.ticket().number() <= 20);
        assertFalse(a.record().repeat());
        assertEquals(a.ticket().number(), a.record().ticketNumber());
        assertEquals(NOW, a.record().dateTime());
    }

    @Test
    void recordIsSavedImmediately() throws Exception {
        Assignment a = service(tickets(20)).assign(IVANOV);

        assertEquals(List.of(a.record()), store.records);
    }

    @Test
    void repeatedChoiceReturnsSameTicketAndAppendsRepeatRow() throws Exception {
        TicketService service = service(tickets(20));

        Assignment first = service.assign(IVANOV);
        Assignment second = service.assign(IVANOV);
        Assignment third = service.assign(IVANOV);

        assertEquals(first.ticket(), second.ticket());
        assertEquals(first.ticket(), third.ticket());
        assertEquals(List.of(false, true, true), store.records.stream().map(ResultRecord::repeat).toList());
        assertEquals(3, store.records.size()); // каждая генерация — новая строка, старые не меняются
        assertEquals(first.record(), store.records.get(0));
    }

    @Test
    void idempotentAcrossRestarts() throws Exception {
        Assignment first = service(tickets(20)).assign(IVANOV);

        // новый экземпляр сервиса и другой генератор случайных чисел, журнал тот же
        TicketService restarted = new TicketService(tickets(20), store, new Random(999), CLOCK);

        assertEquals(first.ticket().number(), restarted.assign(IVANOV).ticket().number());
    }

    @Test
    void ticketComesFromVeryFirstRecordOfStudent() throws Exception {
        store.records.add(new ResultRecord("TI-237", "Иванов", "Иван", 5, NOW.minusDays(2), false));
        store.records.add(new ResultRecord("TI-237", "Иванов", "Иван", 9, NOW.minusDays(1), true));

        assertEquals(5, service(tickets(20)).assign(IVANOV).ticket().number());
    }

    @Test
    void studentLookupIgnoresCaseAndSurroundingSpaces() throws Exception {
        store.records.add(new ResultRecord("ti-237", " иванов ", "ИВАН", 5, NOW, false));

        Assignment a = service(tickets(20)).assign(IVANOV);

        assertEquals(5, a.ticket().number());
        assertTrue(a.record().repeat());
    }

    @Test
    void sameNameInAnotherGroupIsAnotherStudent() throws Exception {
        store.records.add(new ResultRecord("TI-238", "Иванов", "Иван", 5, NOW, false));

        assertFalse(service(tickets(20)).assign(IVANOV).record().repeat());
    }

    @Test
    void otherStudentsDoNotAffectEachOther() throws Exception {
        TicketService service = service(tickets(20));
        Student petrov = new Student("TI-237", "Петров", "Пётр");

        service.assign(IVANOV);
        Assignment p = service.assign(petrov);

        assertFalse(p.record().repeat());
        assertEquals(2, store.records.size());
    }

    @Test
    void drawsOnlyFromValidTicketsWhenSomeWereSkipped() throws Exception {
        TicketService service = service(tickets(1, 2, 4)); // билет 3 отброшен при разборе
        Set<Integer> seen = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            seen.add(service.assign(new Student("TI-237", "Студент" + i, "Имя")).ticket().number());
        }

        assertEquals(Set.of(1, 2, 4), seen);
    }

    @Test
    void unavailableJournalIsReportedAndNothingIsSaved() {
        store.appendFailures = 1;

        assertThrows(IOException.class, () -> service(tickets(20)).assign(IVANOV));

        assertTrue(store.records.isEmpty());
    }

    @Test
    void failsWithMessageIfJournalTicketIsMissingInDocument() {
        store.records.add(new ResultRecord("TI-237", "Иванов", "Иван", 99, NOW, false));
        TicketService service = service(tickets(20));

        var e = assertThrows(TicketUnavailableException.class, () -> service.assign(IVANOV));

        assertTrue(e.getMessage().contains("99"));
        assertEquals(1, store.records.size()); // ничего не добавлено
    }

    @Test
    void rejectsEmptyTicketList() {
        assertThrows(IllegalArgumentException.class, () -> service(List.of()));
    }
}
