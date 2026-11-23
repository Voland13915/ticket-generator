package tickets.ui;

import tickets.model.Assignment;
import tickets.model.Student;
import tickets.service.TicketUnavailableException;
import tickets.service.TicketsController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;

/** Главное окно: показывает состояние контроллера и передаёт ему действия пользователя. */
public final class MainFrame extends JFrame {
    private final TicketsController controller;
    private final String journalName;
    private final MainView view = new MainView();
    private List<String> warnings = List.of();

    /** @param journalName имя файла журнала — только для сообщений пользователю */
    public MainFrame(TicketsController controller, String journalName) {
        super("Генератор экзаменационных билетов");
        this.controller = controller;
        this.journalName = journalName;

        view.onRefresh(this::reload);
        view.onToggleTheme(this::toggleTheme);
        view.onShowWarnings(() -> Dialogs.showWarnings(this, warnings));
        view.selection.onGenerate(this::generate);
        view.setDarkTheme(Theme.isDark());

        setContentPane(view);
        getRootPane().setDefaultButton(view.selection.generateButton);
        Keys.bind(getRootPane(), KeyEvent.VK_F5, this::reload);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1060, 700);
        setMinimumSize(new Dimension(900, 640));
        setLocationRelativeTo(null);
        reload();
    }

    private void reload() {
        TicketsController.Snapshot snapshot = controller.reload();
        warnings = snapshot.warnings();
        view.showSnapshot(snapshot);
    }

    private void toggleTheme() {
        Theme.toggle();
        view.setDarkTheme(Theme.isDark());
    }

    private void generate(Student student) {
        try {
            Optional<Assignment> result =
                    controller.generate(student, error -> Dialogs.confirmRetry(this, journalName, error));
            if (result.isEmpty()) {
                view.setStatus("Сохранение отменено, билет не выдан.");
                return;
            }
            Assignment assignment = result.get();
            view.addRecord(assignment.record());
            view.setStatus("Билет № " + assignment.ticket().number()
                    + (assignment.record().repeat() ? " выдан повторно: " : " выдан: ")
                    + student.displayName() + ", " + student.group() + ".");

            new ResultDialog(this, student, assignment).setVisible(true);
            view.selection.resetStudent();
        } catch (TicketUnavailableException e) {
            Dialogs.showError(this, "Билет недоступен", e.getMessage());
        }
    }
}
