package tickets.console;

import tickets.io.XlsxLog;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** journal.xlsx из Лаб 1: Last name, First name, номер билета, дата и время. */
final class Journal implements EntryLog {
    static final List<String> HEADER = List.of("Last name", "First name", "Номер билета", "Дата и время");

    private final XlsxLog log;

    Journal(Path file) {
        this.log = new XlsxLog(file, "Журнал", HEADER);
    }

    @Override
    public void append(Entry entry) throws IOException {
        log.append(List.of(entry.lastName(), entry.firstName(), entry.ticket(), entry.dateTime()));
    }
}
