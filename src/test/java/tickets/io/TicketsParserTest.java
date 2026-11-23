package tickets.io;

import org.junit.jupiter.api.Test;
import tickets.io.TicketsParser.Paragraph;
import tickets.model.Ticket;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketsParserTest {

    private static Paragraph p(String text) {
        return new Paragraph(text, false);
    }

    private static Paragraph listItem(String text) {
        return new Paragraph(text, true);
    }

    @Test
    void parsesTicketsInFormatFromAssignment() {
        var result = TicketsParser.parse(List.of(
                p("Билет 1"), p("1. Вопрос первый"), p("2. Вопрос второй"), p("3. Вопрос третий"), p(""),
                p("Билет 2"), p("1. Раз"), p("2. Два"), p("3. Три")));

        assertEquals(List.of(
                new Ticket(1, List.of("Вопрос первый", "Вопрос второй", "Вопрос третий")),
                new Ticket(2, List.of("Раз", "Два", "Три"))), result.tickets());
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void acceptsHeaderVariants() {
        var result = TicketsParser.parse(List.of(
                p("билет 5"), p("1) a"), p("2) b"), p("3) c"),
                p("Билет №6"), p("1. a"), p("2. b"), p("3. c"),
                p("Билет 7:"), p("1. a"), p("2. b"), p("3. c")));

        assertEquals(List.of(5, 6, 7), result.tickets().stream().map(Ticket::number).toList());
    }

    @Test
    void skipsTicketWithFewerThanThreeQuestions() {
        var result = TicketsParser.parse(List.of(
                p("Билет 1"), p("1. a"), p("2. b"), p("3. c"),
                p("Билет 2"), p("1. a"), p("2. b"),
                p("Билет 3"), p("1. a"), p("2. b"), p("3. c")));

        assertEquals(List.of(1, 3), result.tickets().stream().map(Ticket::number).toList());
        assertEquals(1, result.warnings().size());
        assertTrue(result.warnings().get(0).contains("Билет 2"));
    }

    @Test
    void skipsUnrecognizedHeaderTogetherWithItsQuestions() {
        var result = TicketsParser.parse(List.of(
                p("Билет 1"), p("1. a"), p("2. b"), p("3. c"),
                p("Билет без номера"), p("1. x"), p("2. y"), p("3. z"),
                p("Билет 3"), p("1. a"), p("2. b"), p("3. c")));

        // вопросы «чужого» билета не должны прилипнуть ни к предыдущему, ни к следующему
        assertEquals(List.of(1, 3), result.tickets().stream().map(Ticket::number).toList());
        assertEquals(List.of("a", "b", "c"), result.tickets().get(0).questions());
        assertEquals(1, result.warnings().size());
        assertTrue(result.warnings().get(0).contains("Билет без номера"));
    }

    @Test
    void rejectsZeroAndHugeTicketNumbers() {
        var result = TicketsParser.parse(List.of(
                p("Билет 0"), p("1. a"), p("2. b"), p("3. c"),
                p("Билет 99999999999999999999"), p("1. a"), p("2. b"), p("3. c")));

        assertTrue(result.tickets().isEmpty());
        assertEquals(2, result.warnings().size());
    }

    @Test
    void keepsFirstOccurrenceOfDuplicateTicket() {
        var result = TicketsParser.parse(List.of(
                p("Билет 1"), p("1. первый"), p("2. b"), p("3. c"),
                p("Билет 1"), p("1. второй"), p("2. b"), p("3. c")));

        assertEquals(1, result.tickets().size());
        assertEquals("первый", result.tickets().get(0).questions().get(0));
        assertEquals(1, result.warnings().size());
    }

    @Test
    void readsWordAutoNumberedQuestions() {
        // Word сам превращает «1. текст» в список и убирает номер из текста абзаца
        var result = TicketsParser.parse(List.of(
                p("Билет 4"), listItem("Первый"), listItem("Второй"), listItem("Третий")));

        assertEquals(List.of("Первый", "Второй", "Третий"), result.tickets().get(0).questions());
    }

    @Test
    void ignoresTitleBlankLinesAndOrphanQuestions() {
        var result = TicketsParser.parse(List.of(
                p("Экзаменационные билеты"), p("  "), p("1. вопрос без билета"), p("2. ещё один"),
                p("Билет 1"), p("1. a"), p("2. b"), p("3. c")));

        assertEquals(1, result.tickets().size());
        assertEquals(1, result.warnings().size()); // про сирот — одно предупреждение, а не по строке
    }

    @Test
    void normalizesNonBreakingSpacesAndInnerWhitespace() {
        var result = TicketsParser.parse(List.of(
                p("Билет\u00A01"), p("1.\u00A0Текст   с   пробелами"), p("2. b"), p("3. c")));

        assertEquals("Текст с пробелами", result.tickets().get(0).questions().get(0));
    }

    @Test
    void emptyDocumentGivesNoTickets() {
        var result = TicketsParser.parse(List.of());

        assertTrue(result.tickets().isEmpty());
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void keepsAllQuestionsWhenThereAreMoreThanThree() {
        var result = TicketsParser.parse(List.of(
                p("Билет 1"), p("1. a"), p("2. b"), p("3. c"), p("4. d")));

        assertEquals(4, result.tickets().get(0).questions().size());
    }
}
