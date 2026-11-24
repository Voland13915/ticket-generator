package tickets.service;

import tickets.io.StudentsSource;
import tickets.io.ParsedTickets;
import tickets.io.TicketsSource;
import tickets.model.Student;
import tickets.model.Ticket;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Собирает каталог из источников студентов и билетов. Любая проблема — сообщение, а не исключение. */
public final class SourceLoader implements CatalogSource {
    private final StudentsSource studentsSource;
    private final TicketsSource ticketsSource;

    public SourceLoader(StudentsSource studentsSource, TicketsSource ticketsSource) {
        this.studentsSource = studentsSource;
        this.ticketsSource = ticketsSource;
    }

    @Override
    public LoadResult load() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Map<String, List<Student>> groups = readStudents(errors);
        List<Ticket> tickets = readTickets(errors, warnings);

        Catalog catalog = errors.isEmpty() ? new Catalog(groups, tickets) : null;
        return new LoadResult(catalog, List.copyOf(errors), List.copyOf(warnings));
    }

    private Map<String, List<Student>> readStudents(List<String> errors) {
        try {
            return studentsSource.read();
        } catch (IOException e) {
            errors.add(describe(studentsSource.name(), e));
            return Map.of();
        }
    }

    private List<Ticket> readTickets(List<String> errors, List<String> warnings) {
        try {
            ParsedTickets parsed = ticketsSource.read();
            warnings.addAll(parsed.warnings());
            if (parsed.tickets().isEmpty()) {
                errors.add("В файле " + ticketsSource.name() + " не найдено ни одного корректного билета.");
            }
            return parsed.tickets();
        } catch (IOException e) {
            errors.add(describe(ticketsSource.name(), e));
            return List.of();
        }
    }

    private static String describe(String sourceName, IOException error) {
        if (error instanceof NoSuchFileException missing) {
            String where = missing.getFile() == null ? "" : " Положите его в " + folderOf(missing.getFile()) + ".";
            return "Не найден файл " + sourceName + "." + where;
        }
        return "Не удалось прочитать " + sourceName + ": " + error.getMessage();
    }

    private static Path folderOf(String file) {
        return Path.of(file).toAbsolutePath().getParent();
    }
}
