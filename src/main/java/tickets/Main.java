package tickets;

import tickets.console.ConsoleApp;
import tickets.io.DocxTicketsSource;
import tickets.io.ResultsJournal;
import tickets.io.XlsxStudentsSource;
import tickets.service.SourceLoader;
import tickets.service.TicketsController;
import tickets.ui.MainFrame;
import tickets.ui.Theme;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Random;

/**
 * Точка входа и единственное место, где собираются конкретные зависимости.
 * Окно (Лаб 2) по умолчанию, консольный режим (Лаб 1) — с ключом --console.
 */
public final class Main {
    private static final Path STUDENTS_FILE = Path.of("students.xlsx");
    private static final Path TICKETS_FILE = Path.of("tickets.docx");
    private static final Path RESULTS_FILE = Path.of("results.xlsx");

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        AppLog.init();
        if (List.of(args).contains("--console")) {
            ConsoleApp.run();
        } else if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Графическое окружение недоступно. Консольный режим: java -jar ticket-generator.jar --console");
        } else {
            SwingUtilities.invokeLater(Main::startGui);
        }
    }

    private static void startGui() {
        Theme.install();
        SourceLoader source = new SourceLoader(
                new XlsxStudentsSource(STUDENTS_FILE), new DocxTicketsSource(TICKETS_FILE));
        TicketsController controller = new TicketsController(
                source, new ResultsJournal(RESULTS_FILE), new Random(), Clock.systemDefaultZone());
        new MainFrame(controller, RESULTS_FILE.getFileName().toString()).setVisible(true);
    }
}
