package tickets.io;

import org.junit.jupiter.api.Test;
import tickets.model.Ticket;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParsedTicketsTest {

    @Test
    void listsAreCopiedAndCannotBeChanged() {
        List<Ticket> tickets = new ArrayList<>(List.of(new Ticket(1, List.of("a", "b", "c"))));
        List<String> warnings = new ArrayList<>(List.of("билет 2 пропущен"));

        ParsedTickets parsed = new ParsedTickets(tickets, warnings);
        tickets.clear();
        warnings.clear();

        assertEquals(1, parsed.tickets().size());
        assertEquals(1, parsed.warnings().size());
        assertThrows(UnsupportedOperationException.class, () -> parsed.tickets().clear());
    }
}
