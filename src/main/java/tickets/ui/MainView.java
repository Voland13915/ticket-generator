package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;
import tickets.model.ResultRecord;
import tickets.service.TicketsController;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/** Содержимое главного окна: шапка, ошибки загрузки, выбор студента, журнал и строка состояния. */
final class MainView extends JPanel {
    final SelectionPanel selection = new SelectionPanel();
    private final JournalPanel journal = new JournalPanel();
    private final ErrorBanner banner = new ErrorBanner();
    private final JLabel subtitle = new JLabel(" ");
    private final JLabel status = new JLabel(" ");
    private final JButton warningsButton = toolButton();
    private final JButton refreshButton = toolButton();
    private final JButton themeButton = toolButton();

    MainView() {
        super(new BorderLayout());
        refreshButton.setText("Обновить данные");
        refreshButton.setToolTipText("Перечитать students.xlsx и tickets.docx (F5)");
        warningsButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "errorText");
        warningsButton.setToolTipText("Билеты, пропущенные при чтении tickets.docx");
        warningsButton.setVisible(false);
        status.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "muted"});

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(banner, BorderLayout.NORTH);
        body.add(selection, BorderLayout.WEST);
        body.add(journal, BorderLayout.CENTER);

        add(header(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        add(statusBar(), BorderLayout.SOUTH);
    }

    void onRefresh(Runnable action) {
        refreshButton.addActionListener(e -> action.run());
        banner.onRetry(action);
    }

    void onToggleTheme(Runnable action) {
        themeButton.addActionListener(e -> action.run());
    }

    void onShowWarnings(Runnable action) {
        warningsButton.addActionListener(e -> action.run());
    }

    void setDarkTheme(boolean dark) {
        themeButton.setText(dark ? "Светлая тема" : "Тёмная тема");
    }

    void showSnapshot(TicketsController.Snapshot snapshot) {
        if (snapshot.ready()) {
            selection.setCatalog(snapshot.catalog().groups());
            banner.clear();
            subtitle.setText(Russian.count(snapshot.catalog().groups().size(), "группа", "группы", "групп") + ", "
                    + Russian.count(snapshot.catalog().tickets().size(), "билет", "билета", "билетов"));
        } else {
            selection.setCatalog(Map.of());
            banner.showErrors(snapshot.errors());
            subtitle.setText("Данные не загружены");
        }

        int warnings = snapshot.warnings().size();
        warningsButton.setVisible(warnings > 0);
        warningsButton.setText(Russian.count(warnings, "предупреждение", "предупреждения", "предупреждений"));

        journal.setRecords(snapshot.history());
        if (!snapshot.ready()) {
            setStatus("Исправьте входные файлы и нажмите «Повторить загрузку».");
        } else if (snapshot.historyError() != null) {
            setStatus("Журнал не прочитан: " + snapshot.historyError());
        } else {
            setStatus("Данные загружены, можно выдавать билеты.");
        }
    }

    void addRecord(ResultRecord record) {
        journal.add(record);
    }

    void setStatus(String text) {
        status.setText(text);
    }

    private JComponent header() {
        JLabel title = new JLabel("Генератор экзаменационных билетов");
        title.putClientProperty(FlatClientProperties.STYLE_CLASS, "h2");
        subtitle.putClientProperty(FlatClientProperties.STYLE_CLASS, "muted");

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);
        actions.add(warningsButton);
        actions.add(refreshButton);
        actions.add(themeButton);

        JPanel header = new EdgePanel(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 14, 16));
        header.add(titles, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JComponent statusBar() {
        JPanel bar = new EdgePanel(true);
        bar.setBorder(BorderFactory.createEmptyBorder(7, 24, 7, 24));
        bar.add(status, BorderLayout.WEST);
        return bar;
    }

    private static JButton toolButton() {
        JButton button = new JButton();
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        return button;
    }

    /** Панель с тонкой линией сверху (строка состояния) или снизу (шапка). */
    private static final class EdgePanel extends JPanel {
        private final boolean lineOnTop;

        EdgePanel(boolean lineOnTop) {
            super(new BorderLayout());
            this.lineOnTop = lineOnTop;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Theme.color("line"));
            int y = lineOnTop ? 0 : getHeight() - 1;
            g.drawLine(0, y, getWidth(), y);
        }
    }
}
