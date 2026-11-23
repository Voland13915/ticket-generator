package tickets.service;

import tickets.io.ResultsStore;
import tickets.model.Assignment;
import tickets.model.ResultRecord;
import tickets.model.Student;
import tickets.model.Ticket;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/** Правила выдачи: новому студенту — случайный билет, повторно пришедшему — тот же, что выпал первым. */
public final class TicketService {
    private final Map<Integer, Ticket> tickets = new LinkedHashMap<>();
    private final List<Integer> numbers;
    private final ResultsStore store;
    private final Random random;
    private final Clock clock;

    public TicketService(List<Ticket> tickets, ResultsStore store, Random random, Clock clock) {
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("Список билетов пуст");
        }
        tickets.forEach(ticket -> this.tickets.put(ticket.number(), ticket));
        this.numbers = List.copyOf(this.tickets.keySet());
        this.store = store;
        this.random = random;
        this.clock = clock;
    }

    /**
     * Назначает билет и сразу дописывает строку в журнал. Одна попытка: если журнал недоступен,
     * бросает IOException и ничего не записывает — решать, повторять ли, должен вызывающий.
     */
    public Assignment assign(Student student) throws IOException, TicketUnavailableException {
        // по контракту ResultsStore записи идут в хронологическом порядке, поэтому findFirst — первая запись студента
        Optional<ResultRecord> first = store.readAll().stream()
                .filter(record -> record.studentKey().equals(student.key()))
                .findFirst();

        int number = first.map(ResultRecord::ticketNumber).orElseGet(this::drawNumber);
        Ticket ticket = tickets.get(number);
        if (ticket == null) {
            throw new TicketUnavailableException(number);
        }

        ResultRecord record = new ResultRecord(student.group(), student.lastName(), student.firstName(),
                number, LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS), first.isPresent());
        store.append(record);
        return new Assignment(ticket, record);
    }

    /** Случайный номер среди корректных билетов: если часть билетов пропущена, «дыр» в выдаче не будет. */
    private int drawNumber() {
        return numbers.get(random.nextInt(numbers.size()));
    }
}
