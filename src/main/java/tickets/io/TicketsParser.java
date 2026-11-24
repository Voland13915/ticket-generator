package tickets.io;

import tickets.model.Ticket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Разбор текста tickets.docx. Работает со списком абзацев, поэтому не зависит от Word и легко тестируется.
 * Некорректные билеты пропускаются, причина попадает в список предупреждений.
 */
public final class TicketsParser {
    public static final int MIN_QUESTIONS = 3;

    private static final int CASE = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    /** «Билет 7», «билет №7», «Билет 7:». */
    private static final Pattern HEADER = Pattern.compile("^Билет\\s*№?\\s*(\\d+)\\s*[.:]?$", CASE);
    /** Строка, которая начинается как заголовок, но не распознана («Билет без номера»). */
    private static final Pattern HEADER_LIKE = Pattern.compile("^Билет(?!\\p{L}).*", CASE);
    /** «1. Текст» или «1) Текст». */
    private static final Pattern QUESTION = Pattern.compile("^\\d+\\s*[.)]\\s*(.+)$");

    /**
     * @param numbered абзац оформлен автоматической нумерацией Word: номера в тексте нет,
     *                 Word хранит его отдельно
     */
    public record Paragraph(String text, boolean numbered) {
    }

    private TicketsParser() {
    }

    public static ParsedTickets parse(List<Paragraph> paragraphs) {
        Parser parser = new Parser();
        paragraphs.forEach(parser::feed);
        parser.closeTicket();
        return new ParsedTickets(parser.tickets, parser.warnings);
    }

    private static final class Parser {
        private final List<Ticket> tickets = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final Set<Integer> accepted = new HashSet<>();
        private final List<String> questions = new ArrayList<>();
        /** Номер билета, который сейчас читаем; null — билета нет. */
        private Integer current;
        /** Идёт билет с нераспознанным заголовком: его вопросы отбрасываем молча. */
        private boolean discarding;
        private boolean orphanReported;

        void feed(Paragraph paragraph) {
            String text = clean(paragraph.text());
            if (text.isEmpty()) {
                return;
            }
            if (!paragraph.numbered()) {
                Matcher header = HEADER.matcher(text);
                if (header.matches()) {
                    startTicket(header.group(1), text);
                    return;
                }
                if (HEADER_LIKE.matcher(text).matches()) {
                    rejectHeader(text);
                    return;
                }
            }
            String question = questionText(paragraph, text);
            if (question == null || discarding) {
                return;
            }
            if (current == null) {
                reportOrphan(question);
            } else {
                questions.add(question);
            }
        }

        void closeTicket() {
            if (current == null) {
                return;
            }
            if (questions.size() < MIN_QUESTIONS) {
                warnings.add("Билет " + current + " пропущен: найдено вопросов — " + questions.size()
                        + ", нужно не меньше " + MIN_QUESTIONS + ".");
            } else if (!accepted.add(current)) {
                warnings.add("Билет " + current + " встречается повторно — повторное вхождение пропущено.");
            } else {
                tickets.add(new Ticket(current, questions));
            }
            current = null;
            questions.clear();
        }

        private void startTicket(String digits, String headerText) {
            closeTicket();
            int number = toNumber(digits);
            if (number < 1) {
                rejectHeader(headerText);
                return;
            }
            current = number;
            discarding = false;
        }

        private void rejectHeader(String text) {
            closeTicket();
            discarding = true;
            warnings.add("Заголовок билета не распознан: «" + text + "». Билет пропущен.");
        }

        private void reportOrphan(String question) {
            if (!orphanReported) {
                warnings.add("Вопрос вне билета пропущен: «" + question + "».");
                orphanReported = true;
            }
        }

        private static String questionText(Paragraph paragraph, String text) {
            if (paragraph.numbered()) {
                return text;
            }
            Matcher matcher = QUESTION.matcher(text);
            return matcher.matches() ? matcher.group(1).strip() : null;
        }

        private static int toNumber(String digits) {
            try {
                return Integer.parseInt(digits);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private static String clean(String text) {
            return text.replace('\u00A0', ' ').replaceAll("\\s+", " ").strip();
        }
    }
}
