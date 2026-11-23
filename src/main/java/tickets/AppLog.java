package tickets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Общий логгер приложения: пишет в консоль и в файл tickets.log. */
public final class AppLog {
    public static final Logger LOG = Logger.getLogger("tickets");

    private AppLog() {
    }

    public static void init() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %5$s%6$s%n");
        try {
            FileHandler file = new FileHandler("tickets.log", true);
            file.setEncoding(StandardCharsets.UTF_8.name());
            file.setFormatter(new SimpleFormatter());
            LOG.addHandler(file);
        } catch (IOException | SecurityException e) {
            LOG.log(Level.WARNING, "Лог-файл tickets.log недоступен, пишем только в консоль", e);
        }
    }
}
