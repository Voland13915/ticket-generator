package tickets.service;

import org.junit.jupiter.api.Test;
import tickets.io.StudentsSource;
import tickets.io.TicketsParser;
import tickets.io.TicketsSource;
import tickets.model.Student;
import tickets.model.Ticket;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Загрузчик проверяется на подставных источниках: Excel и Word для этого не нужны. */
class SourceLoaderTest {
    private static final Map<String, List<Student>> GROUPS = Map.of("TI-237", List.of(new Student("TI-237", "Иванов", "Иван")));
    private static final TicketsParser.Result ONE_TICKET =
            new TicketsParser.Result(List.of(new Ticket(1, List.of("a", "b", "c"))), List.of("билет 2 пропущен"));

    private record FakeStudents(String name, Map<String, List<Student>> groups, IOException error) implements StudentsSource {
        @Override
        public Map<String, List<Student>> read() throws IOException {
            if (error != null) throw error;
            return groups;
        }
    }

    private record FakeTickets(String name, TicketsParser.Result result, IOException error) implements TicketsSource {
        @Override
        public TicketsParser.Result read() throws IOException {
            if (error != null) throw error;
            return result;
        }
    }

    private static FakeStudents goodStudents() {
        return new FakeStudents("students.xlsx", GROUPS, null);
    }

    private static FakeTickets goodTickets() {
        return new FakeTickets("tickets.docx", ONE_TICKET, null);
    }

    @Test
    void buildsCatalogAndPassesWarningsThrough() {
        LoadResult result = new SourceLoader(goodStudents(), goodTickets()).load();

        assertTrue(result.ok());
        assertEquals(GROUPS, result.catalog().groups());
        assertEquals(ONE_TICKET.tickets(), result.catalog().tickets());
        assertEquals(List.of("билет 2 пропущен"), result.warnings());
    }

    @Test
    void reportsBothMissingSourcesAtOnce() {
        LoadResult result = new SourceLoader(
                new FakeStudents("students.xlsx", null, new NoSuchFileException("students.xlsx")),
                new FakeTickets("tickets.docx", null, new NoSuchFileException("tickets.docx"))).load();

        assertFalse(result.ok());
        assertNull(result.catalog());
        assertEquals(2, result.errors().size());
        assertTrue(result.errors().get(0).startsWith("Не найден файл students.xlsx."));
        assertTrue(result.errors().get(1).startsWith("Не найден файл tickets.docx."));
    }

    @Test
    void reportsOtherReadFailuresWithCause() {
        LoadResult result = new SourceLoader(
                new FakeStudents("students.xlsx", null, new IOException("повреждён")), goodTickets()).load();

        assertEquals(List.of("Не удалось прочитать students.xlsx: повреждён"), result.errors());
    }

    @Test
    void errorIfNoValidTicketsLeft() {
        LoadResult result = new SourceLoader(goodStudents(),
                new FakeTickets("tickets.docx", new TicketsParser.Result(List.of(), List.of("билет 1 пропущен")), null)).load();

        assertFalse(result.ok());
        assertEquals(List.of("В файле tickets.docx не найдено ни одного корректного билета."), result.errors());
        assertEquals(List.of("билет 1 пропущен"), result.warnings());
    }
}
