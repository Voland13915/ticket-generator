package tickets.service;

import tickets.AppLog;
import tickets.io.ResultsStore;
import tickets.model.Assignment;
import tickets.model.ResultRecord;
import tickets.model.Student;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Сценарии приложения: загрузка данных и выдача билета с повтором при занятом файле.
 * Окно только показывает результат и спрашивает пользователя — всё остальное решает контроллер,
 * поэтому его можно проверять без Swing.
 */
public final class TicketsController {
    private static final Logger LOG = AppLog.LOG;

    /**
     * Состояние после загрузки.
     *
     * @param historyError null, если журнал прочитан; иначе причина (приложение при этом работает дальше)
     */
    public record Snapshot(Catalog catalog, List<String> errors, List<String> warnings,
                           List<ResultRecord> history, String historyError) {

        public boolean ready() {
            return errors.isEmpty();
        }
    }

    private final CatalogSource source;
    private final ResultsStore store;
    private final Random random;
    private final Clock clock;
    /** null, пока входные данные не загружены без ошибок. */
    private TicketService service;

    public TicketsController(CatalogSource source, ResultsStore store, Random random, Clock clock) {
        this.source = source;
        this.store = store;
        this.random = random;
        this.clock = clock;
    }

    /** Перечитывает входные файлы и журнал. */
    public Snapshot reload() {
        LoadResult loaded = source.load();
        loaded.errors().forEach(LOG::severe);
        loaded.warnings().forEach(LOG::warning);
        service = loaded.ok() ? new TicketService(loaded.catalog().tickets(), store, random, clock) : null;

        List<ResultRecord> history = List.of();
        String historyError = null;
        try {
            history = store.readAll();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Не удалось прочитать журнал", e);
            historyError = e.getMessage();
        }
        return new Snapshot(loaded.catalog(), loaded.errors(), loaded.warnings(), history, historyError);
    }

    /**
     * Выдаёт билет. Если журнал недоступен (например, открыт в Excel), спрашивает {@code retry}:
     * true — повторить, false — отменить.
     *
     * @return итог выдачи или пустой Optional, если пользователь отменил повтор (в журнал ничего не пишется)
     * @throws IllegalStateException если входные данные не загружены
     */
    public Optional<Assignment> generate(Student student, Predicate<IOException> retry)
            throws TicketUnavailableException {
        if (service == null) {
            throw new IllegalStateException("Входные файлы не загружены");
        }
        while (true) {
            try {
                Assignment assignment = service.assign(student);
                LOG.info("Выдан билет № " + assignment.ticket().number() + ": " + student.group() + ", "
                        + student.displayName() + (assignment.record().repeat() ? " (повтор)" : ""));
                return Optional.of(assignment);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Не удалось сохранить запись в журнал", e);
                if (!retry.test(e)) {
                    return Optional.empty();
                }
            }
        }
    }
}
