package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;
import tickets.model.ResultRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/** Журнал выдачи: сводка и таблица всех записей в хронологическом порядке. */
final class JournalPanel extends JPanel {
    private static final int[] COLUMN_WIDTHS = {90, 170, 140, 70, 170, 80};
    /** Узкие колонки не сжимаются: при маленьком окне сужаются фамилия и имя. */
    private static final int[] MIN_WIDTHS = {70, 90, 80, 64, 150, 72};

    private final HistoryTableModel model = new HistoryTableModel();
    private final JTable table = new JournalTable(model);
    private final JLabel stats = new JLabel(" ");

    JournalPanel() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(22, 24, 16, 24));

        JLabel title = new JLabel("Журнал выдачи");
        title.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        stats.putClientProperty(FlatClientProperties.STYLE_CLASS, "muted");
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(title, BorderLayout.WEST);
        head.add(stats, BorderLayout.EAST);

        table.setFillsViewportHeight(true);
        table.setRowSelectionAllowed(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new TextRenderer());
        table.getColumnModel().getColumn(HistoryTableModel.REPEAT_COLUMN).setCellRenderer(new RepeatRenderer());
        TableCellRenderer header = new HeaderRenderer();
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
            table.getColumnModel().getColumn(i).setMinWidth(MIN_WIDTHS[i]);
            table.getColumnModel().getColumn(i).setHeaderRenderer(header);
        }

        add(head, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    void setRecords(List<ResultRecord> records) {
        model.setRecords(records);
        changed();
    }

    void add(ResultRecord record) {
        model.add(record);
        changed();
    }

    private void changed() {
        stats.setText(JournalStats.of(model.records()).describe());
        int last = model.getRowCount() - 1;
        if (last >= 0) {
            table.scrollRectToVisible(table.getCellRect(last, 0, true));
        }
    }

    private static boolean centered(int column) {
        return column == HistoryTableModel.TICKET_COLUMN || column == HistoryTableModel.REPEAT_COLUMN;
    }

    /**
     * Заголовки выровнены так же, как данные под ними. Стандартный рендерер берётся при каждом вызове,
     * поэтому после смены темы шапка перерисуется в новых цветах.
     */
    private static final class HeaderRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            Component cell = t.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(t, value, selected, focus, row, column);
            if (cell instanceof JLabel label) {
                label.setHorizontalAlignment(centered(column) ? SwingConstants.CENTER : SwingConstants.LEADING);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            }
            return cell;
        }
    }

    /** Таблица с подсказкой вместо пустоты, пока записей нет. */
    private static final class JournalTable extends JTable {
        JournalTable(HistoryTableModel model) {
            super(model);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() > 0) {
                return;
            }
            Graphics2D g2 = Theme.smooth(g);
            g2.setColor(Theme.color("muted"));
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String[] lines = {"Журнал пока пуст.", "Выберите группу и студента слева, чтобы выдать первый билет."};
            int y = getVisibleRect().y + getVisibleRect().height / 2 - fm.getHeight();
            for (String line : lines) {
                g2.drawString(line, (getWidth() - fm.stringWidth(line)) / 2, y);
                y += fm.getHeight() + 4;
            }
            g2.dispose();
        }
    }

    /** Отступы в ячейках, жирный номер билета, приглушённая дата. */
    private static final class TextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            super.getTableCellRendererComponent(t, value, selected, false, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setFont(column == HistoryTableModel.TICKET_COLUMN ? t.getFont().deriveFont(Font.BOLD) : t.getFont());
            setHorizontalAlignment(centered(column) ? CENTER : LEFT);
            if (!selected) {
                setForeground(column == HistoryTableModel.DATE_COLUMN ? Theme.color("muted") : t.getForeground());
            }
            return this;
        }
    }

    /** «да» — янтарный бейдж, «нет» — приглушённый текст. */
    private final class RepeatRenderer implements TableCellRenderer {
        private final JPanel cell = new JPanel(new GridBagLayout());
        private final Pill badge = new Pill("");

        RepeatRenderer() {
            cell.add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            boolean repeat = model.recordAt(row).repeat();
            // без выделения ячейка прозрачна: иначе она закроет линию между строками
            cell.setOpaque(selected);
            cell.setBackground(t.getSelectionBackground());
            badge.setText(String.valueOf(value));
            badge.setFont(t.getFont());
            badge.setColors(repeat ? "repeatBackground" : null, null);
            badge.setForeground(selected && !repeat ? t.getSelectionForeground()
                    : Theme.color(repeat ? "repeatText" : "muted"));
            return cell;
        }
    }
}
