package tickets.ui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/** Стандартные диалоги окна. */
final class Dialogs {

    private Dialogs() {
    }

    /** Журнал недоступен (обычно открыт в Excel): предлагает закрыть файл и повторить. true — повторить. */
    static boolean confirmRetry(Component parent, String journalName, IOException error) {
        Object[] options = {"Повторить", "Отмена"};
        int choice = JOptionPane.showOptionDialog(parent,
                "<html><body style='width: 380px'>Не удалось сохранить запись в " + Html.escape(journalName)
                        + ":<br>" + Html.escape(String.valueOf(error.getMessage()))
                        + "<br><br>Если файл открыт в Excel — закройте его и нажмите «Повторить».</body></html>",
                "Файл журнала недоступен", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        return choice == 0;
    }

    static void showWarnings(Component parent, List<String> warnings) {
        JTextArea area = new JTextArea(String.join("\n", warnings));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(560, 220));
        JOptionPane.showMessageDialog(parent, scroll, "Предупреждения при чтении файлов", JOptionPane.WARNING_MESSAGE);
    }

    static void showError(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
