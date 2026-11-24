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
    private final StatusBar status;
    private final JLabel subtitle = new JLabel(" ");
    private final JButton warningsButton = new JButton();
    private final JButton refreshButton = new JButton("Обновить");
    private final JButton themeButton = new JButton();

    /** @param journalName имя файла журнала — показывается в строке состояния */
    MainView(String journalName) {
        super(new BorderLayout());
        status = new StatusBar(journalName);

        warningsButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "chip");
        warningsButton.setIcon(Icons.of(Icons.Kind.WARNING, 14, "repeatText"));
        warningsButton.setToolTipText("Билеты, пропущенные при чтении tickets.docx");
        warningsButton.setVisible(false);

        refreshButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "quiet");
        refreshButton.setIcon(Icons.of(Icons.Kind.REFRESH, 16, "ink"));
        refreshButton.setToolTipText("Перечитать students.xlsx и tickets.docx (F5)");

        themeButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "quiet");

        // кнопки шапки работают мышью и горячими клавишами; фокус им не нужен, иначе на старте вокруг первой рамка
        for (JButton button : new JButton[]{warningsButton, refreshButton, themeButton}) {
            button.setFocusable(false);
        }

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(banner, BorderLayout.NORTH);
        body.add(selection, BorderLayout.WEST);
        body.add(journal, BorderLayout.CENTER);

        add(header(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
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
        themeButton.setIcon(Icons.of(dark ? Icons.Kind.SUN : Icons.Kind.MOON, 16, "ink"));
        themeButton.setToolTipText(dark ? "Светлая тема" : "Тёмная тема");
    }

    void showSnapshot(TicketsController.Snapshot snapshot) {
        if (snapshot.ready()) {
            selection.setCatalog(snapshot.catalog().groups());
            banner.clear();
            subtitle.setText(Russian.count(snapshot.catalog().groups().size(), "группа", "группы", "групп") + "  ·  "
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
            setStatus("Исправьте входные файлы и нажмите «Повторить загрузку».", StatusBar.Tone.ERROR);
        } else if (snapshot.historyError() != null) {
            setStatus("Журнал не прочитан: " + snapshot.historyError(), StatusBar.Tone.WARNING);
        } else {
            setStatus("Данные загружены, можно выдавать билеты.", StatusBar.Tone.OK);
        }
    }

    void addRecord(ResultRecord record) {
        journal.add(record);
    }

    void setStatus(String text, StatusBar.Tone tone) {
        status.show(text, tone);
    }

    private JComponent header() {
        JLabel title = new JLabel("Генератор экзаменационных билетов");
        title.putClientProperty(FlatClientProperties.STYLE_CLASS, "h2");
        subtitle.putClientProperty(FlatClientProperties.STYLE_CLASS, "muted");

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 0));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);

        JPanel brand = new JPanel(new BorderLayout(14, 0));
        brand.setOpaque(false);
        brand.add(new BrandMark(), BorderLayout.WEST);
        brand.add(titles, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(warningsButton);
        actions.add(refreshButton);
        actions.add(themeButton);
        JPanel actionsHolder = new JPanel(new GridBagLayout());
        actionsHolder.setOpaque(false);
        actionsHolder.add(actions);

        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Theme.color("line"));
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 18));
        header.add(brand, BorderLayout.WEST);
        header.add(actionsHolder, BorderLayout.EAST);
        return header;
    }

    /** Фирменный знак слева от заголовка. */
    private static final class BrandMark extends JComponent {
        private static final int SIZE = 42;

        BrandMark() {
            setPreferredSize(new Dimension(SIZE, SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.smooth(g);
            int y = (getHeight() - SIZE) / 2;
            Icons.paintBrand(g2, 0, y, SIZE, Theme.color("primary"), Theme.color("onPrimary"));
            g2.dispose();
        }
    }
}
