package tickets.console;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Сценарий Лаб 1 без терминала и без Excel: ввод подставной, журнал в памяти. */
class ConsoleAppTest {

    /** Ввод по сценарию: когда значения кончились, readField возвращает null — как нажатие ESC. */
    private static final class ScriptedPrompt implements Prompt {
        final Deque<String> fields = new ArrayDeque<>();
        final Deque<Boolean> confirmations = new ArrayDeque<>();
        final List<String> output = new ArrayList<>();

        ScriptedPrompt fields(String... values) {
            fields.addAll(Arrays.asList(values));
            return this;
        }

        @Override
        public void println(String text) {
            output.add(text);
        }

        @Override
        public String readField(String label) {
            return fields.poll();
        }

        @Override
        public boolean awaitEnterOrEsc() {
            Boolean answer = confirmations.poll();
            return answer != null && answer;
        }
    }

    private static final class MemoryLog implements EntryLog {
        final List<Entry> entries = new ArrayList<>();
        int failures;

        @Override
        public void append(Entry entry) throws IOException {
            if (failures > 0) {
                failures--;
                throw new IOException("файл занят");
            }
            entries.add(entry);
        }
    }

    private final MemoryLog log = new MemoryLog();

    private void run(ScriptedPrompt prompt) throws IOException {
        new ConsoleApp(prompt, log, new Random(1), Clock.fixed(Instant.parse("2026-09-30T08:24:38Z"), ZoneOffset.UTC)).loop();
    }

    @Test
    void threeStudentsThenEscGivesThreeEntries() throws IOException {
        run(new ScriptedPrompt().fields("Migov", "Daniil", "Poddubnii", "Roman", "Tihanina", "Polina"));

        assertEquals(List.of("Migov", "Poddubnii", "Tihanina"), log.entries.stream().map(Entry::lastName).toList());
        assertEquals(List.of("Daniil", "Roman", "Polina"), log.entries.stream().map(Entry::firstName).toList());
    }

    @Test
    void showsHintAndPrintsTicketNumber() throws IOException {
        ScriptedPrompt prompt = new ScriptedPrompt().fields("Migov", "Daniil");

        run(prompt);

        assertEquals("Для выхода нажмите ESC.", prompt.output.get(0));
        assertTrue(prompt.output.contains("Билет № " + log.entries.get(0).ticket()));
    }

    @Test
    void ticketsAreInRangeFromOneToTwenty() throws IOException {
        ScriptedPrompt prompt = new ScriptedPrompt();
        for (int i = 0; i < 300; i++) {
            prompt.fields("Фамилия" + i, "Имя");
        }

        run(prompt);

        assertEquals(300, log.entries.size());
        assertTrue(log.entries.stream().allMatch(e -> e.ticket() >= 1 && e.ticket() <= 20));
        assertEquals(20, log.entries.stream().map(Entry::ticket).distinct().count());
    }

    @Test
    void escOnFirstNameEndsWithoutSavingAnything() throws IOException {
        run(new ScriptedPrompt().fields("Migov"));

        assertTrue(log.entries.isEmpty());
    }

    @Test
    void entryHasCurrentTimeWithoutFraction() throws IOException {
        run(new ScriptedPrompt().fields("Migov", "Daniil"));

        assertEquals(java.time.LocalDateTime.ofInstant(Instant.parse("2026-09-30T08:24:38Z"), ZoneOffset.systemDefault()),
                log.entries.get(0).dateTime());
    }

    @Test
    void lockedJournalIsRetriedAfterEnter() throws IOException {
        log.failures = 2;
        ScriptedPrompt prompt = new ScriptedPrompt().fields("Migov", "Daniil");
        prompt.confirmations.addAll(List.of(true, true));

        run(prompt);

        assertEquals(1, log.entries.size());
        assertTrue(prompt.output.stream().anyMatch(line -> line.contains("файл занят")));
    }

    @Test
    void escDuringRetryEndsWithoutSavingTheEntry() throws IOException {
        log.failures = 1;
        ScriptedPrompt prompt = new ScriptedPrompt().fields("Migov", "Daniil", "Poddubnii", "Roman");
        prompt.confirmations.add(false);

        run(prompt);

        assertTrue(log.entries.isEmpty());
        assertEquals(2, prompt.fields.size()); // следующий студент даже не запрашивался
    }
}
