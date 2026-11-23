package tickets.console;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/** Консольный генератор билетов из Лаб 1 (запуск с ключом --console). */
public final class ConsoleApp {
    private static final int MAX_TICKET = 20;

    private final Prompt prompt;
    private final EntryLog log;
    private final Random random;
    private final Clock clock;

    ConsoleApp(Prompt prompt, EntryLog log, Random random, Clock clock) {
        this.prompt = prompt;
        this.log = log;
        this.random = random;
        this.clock = clock;
    }

    /** Собирает настоящие зависимости (терминал, journal.xlsx) и запускает режим. */
    public static void run() throws IOException {
        try (ConsoleInput input = new ConsoleInput()) {
            new ConsoleApp(input, new Journal(Path.of("journal.xlsx")), new Random(), Clock.systemDefaultZone()).loop();
        }
    }

    void loop() throws IOException {
        prompt.println("Для выхода нажмите ESC.");

        while (true) {
            String lastName = prompt.readField("Last name: ");
            if (lastName == null) break;

            String firstName = prompt.readField("First name: ");
            if (firstName == null) break;

            int ticket = random.nextInt(MAX_TICKET) + 1;
            prompt.println("Билет № " + ticket);

            LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
            if (!save(new Entry(lastName, firstName, ticket, now))) break;
            prompt.println("");
        }
        prompt.println("Работа завершена.");
    }

    /** Сохраняет запись; если файл занят — просит закрыть его и повторяет. false — пользователь нажал ESC. */
    private boolean save(Entry entry) throws IOException {
        while (true) {
            try {
                log.append(entry);
                return true;
            } catch (IOException e) {
                prompt.println("Не удалось записать в журнал: " + e.getMessage());
                prompt.println("Если файл открыт в Excel — закройте его.");
                prompt.println("Нажмите Enter для повтора или ESC для выхода (запись не будет сохранена).");
                if (!prompt.awaitEnterOrEsc()) return false;
            }
        }
    }
}
