package tickets.ui;

import tickets.model.ResultRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Set;

/** Таблица журнала выдачи: все записи в хронологическом порядке, повторные выдачи подсвечены. */
final class HistoryPanel extends JPanel {
    private static final Color REPEAT_ROW = new Color(0xFFF4D6);
    private static final Set<Integer> CENTERED_COLUMNS =
            Set.of(HistoryTableModel.TICKET_COLUMN, HistoryTableModel.REPEAT_COLUMN);
    private static final int[] COLUMN_WIDTHS = {90, 170, 140, 70, 160, 70};

    private final HistoryTableModel model = new HistoryTableModel();
    private final JTable table = new JTable(model);

    HistoryPanel(String journalName) {
        super(new BorderLayout());
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new RepeatRowRenderer());
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder(
                "Журнал выдачи (" + journalName + ") — повторные выдачи выделены"));
        add(scroll, BorderLayout.CENTER);
    }

    void setRecords(List<ResultRecord> records) {
        model.setRecords(records);
        scrollToEnd();
    }

    void add(ResultRecord record) {
        model.add(record);
        scrollToEnd();
    }

    private void scrollToEnd() {
        int last = model.getRowCount() - 1;
        if (last >= 0) {
            table.scrollRectToVisible(table.getCellRect(last, 0, true));
        }
    }

    /** Центрирует узкие колонки и подсвечивает повторные выдачи. */
    private final class RepeatRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
            setHorizontalAlignment(CENTERED_COLUMNS.contains(column) ? CENTER : LEFT);
            if (!selected) {
                cell.setBackground(model.recordAt(row).repeat() ? REPEAT_ROW : t.getBackground());
            }
            return cell;
        }
    }
}
