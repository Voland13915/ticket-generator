package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;
import tickets.model.ResultRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/** Журнал выдачи: карточка со сводкой и таблицей всех записей в хронологическом порядке. */
final class JournalPanel extends JPanel {
    private static final int[] COLUMN_WIDTHS = {90, 170, 140, 70, 170, 90};
    /** Узкие колонки не сжимаются: при маленьком окне сужаются фамилия и имя. */
    private static final int[] MIN_WIDTHS = {70, 90, 80, 64, 150, 80};
    private static final int ARC = 16;

    private final HistoryTableModel model = new HistoryTableModel();
    private final JTable table = new JournalTable(model);
    private final Pill issued = chip();
    private final Pill students = chip();
    private final Pill repeats = chip();

    JournalPanel() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(22, 24, 20, 24));

        JLabel title = new JLabel("Журнал выдачи");
        title.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        chips.setOpaque(false);
        chips.add(issued);
        chips.add(students);
        chips.add(repeats);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 16));
        head.add(title, BorderLayout.WEST);
        head.add(chips, BorderLayout.EAST);

        table.setFillsViewportHeight(true);
        table.setRowSelectionAllowed(true);
        table.setFocusable(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.setDefaultRenderer(Object.class, new TextRenderer());
        table.getColumnModel().getColumn(HistoryTableModel.TICKET_COLUMN).setCellRenderer(new BadgeRenderer(false));
        table.getColumnModel().getColumn(HistoryTableModel.REPEAT_COLUMN).setCellRenderer(new BadgeRenderer(true));
        TableCellRenderer header = new HeaderRenderer();
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
            table.getColumnModel().getColumn(i).setMinWidth(MIN_WIDTHS[i]);
            table.getColumnModel().getColumn(i).setHeaderRenderer(header);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        JPanel card = new Card();
        card.add(head, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    void setRecords(List<ResultRecord> records) {
        model.setRecords(records);
        changed();
    }

    /** Новая запись выделяется и прокручивается в видимую область. */
    void add(ResultRecord record) {
        model.add(record);
        changed();
        int last = model.getRowCount() - 1;
        table.setRowSelectionInterval(last, last);
    }

    private void changed() {
        JournalStats stats = JournalStats.of(model.records());
        issued.setText(Russian.count(stats.issued(), "выдача", "выдачи", "выдач"));
        students.setText(Russian.count(stats.students(), "студент", "студента", "студентов"));
        repeats.setText(Russian.count(stats.repeats(), "повтор", "повтора", "повторов"));
        repeats.setColors(stats.repeats() > 0 ? "repeatBackground" : "background", null);
        repeats.setForeground(Theme.color(stats.repeats() > 0 ? "repeatText" : "muted"));
        for (Pill chip : new Pill[]{issued, students, repeats}) {
            chip.setToolTipText(stats.describe());
        }
        boolean any = stats.issued() > 0;
        issued.setVisible(any);
        students.setVisible(any);
        repeats.setVisible(any);

        int last = model.getRowCount() - 1;
        if (last >= 0) {
            table.scrollRectToVisible(table.getCellRect(last, 0, true));
        }
    }

    private static Pill chip() {
        Pill pill = new Pill("");
        pill.setColors("background", null);
        pill.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "muted"});
        pill.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return pill;
    }

    private static boolean centered(int column) {
        return column == HistoryTableModel.TICKET_COLUMN || column == HistoryTableModel.REPEAT_COLUMN;
    }

    /** Фон строки: чередование полос, выделенная строка — цвет выделения. */
    private static Color rowBackground(JTable t, int row, boolean selected) {
        if (selected) {
            return t.getSelectionBackground();
        }
        return row % 2 == 1 ? Theme.color("stripe") : t.getBackground();
    }

    /** Белая карточка со скруглёнными углами и тонкой рамкой. */
    private static final class Card extends JPanel {
        Card() {
            super(new BorderLayout());
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.smooth(g);
            g2.setColor(Theme.color("paper"));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.setColor(Theme.color("line"));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.dispose();
        }
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
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                label.setFont(t.getFont().deriveFont(Font.BOLD, t.getFont().getSize2D() - 1f));
                label.setForeground(Theme.color("muted"));
            }
            return cell;
        }
    }

    /** Таблица с подсказкой и значком вместо пустоты, пока записей нет. */
    private static final class JournalTable extends JTable {
        private static final Icon EMPTY = Icons.of(Icons.Kind.TICKET, 44, "line");

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
            Rectangle view = getVisibleRect();
            int top = view.y + view.height / 2 - 60;
            EMPTY.paintIcon(this, g2, (getWidth() - EMPTY.getIconWidth()) / 2, top);

            Font bold = getFont().deriveFont(Font.BOLD, getFont().getSize2D() + 1f);
            int y = top + EMPTY.getIconHeight() + 22;
            y = centeredLine(g2, "Журнал пока пуст", bold, Theme.color("ink"), y);
            centeredLine(g2, "Выберите группу и студента слева, чтобы выдать первый билет.",
                    getFont(), Theme.color("muted"), y + 6);
            g2.dispose();
        }

        private int centeredLine(Graphics2D g2, String text, Font font, Color color, int y) {
            g2.setFont(font);
            g2.setColor(color);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, y);
            return y + fm.getHeight();
        }
    }

    /** Отступы в ячейках, полосы, приглушённые дата и группа. */
    private static final class TextRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            super.getTableCellRendererComponent(t, value, selected, false, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setFont(t.getFont());
            setHorizontalAlignment(centered(column) ? CENTER : LEFT);
            setBackground(rowBackground(t, row, selected));
            boolean secondary = column == HistoryTableModel.DATE_COLUMN || column == 0;
            setForeground(secondary ? Theme.color("muted") : t.getForeground());
            return this;
        }
    }

    /**
     * Номер билета — синий бейдж; «Повтор»: «да» — янтарный бейдж, «нет» — приглушённый текст.
     * Ячейка непрозрачная, чтобы полосы строк не прерывались.
     */
    private final class BadgeRenderer implements TableCellRenderer {
        private final boolean repeatColumn;
        private final JPanel cell = new JPanel(new GridBagLayout());
        private final Pill badge = new Pill("");

        BadgeRenderer(boolean repeatColumn) {
            this.repeatColumn = repeatColumn;
            cell.add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            cell.setBackground(rowBackground(t, row, selected));
            badge.setText(String.valueOf(value));
            if (repeatColumn) {
                boolean repeat = model.recordAt(row).repeat();
                badge.setFont(t.getFont().deriveFont(repeat ? Font.BOLD : Font.PLAIN));
                badge.setColors(repeat ? "repeatBackground" : null, null);
                badge.setForeground(Theme.color(repeat ? "repeatText" : "muted"));
            } else {
                badge.setFont(t.getFont().deriveFont(Font.BOLD));
                badge.setColors("badge", null);
                badge.setForeground(Theme.color("ink"));
            }
            return cell;
        }
    }
}
