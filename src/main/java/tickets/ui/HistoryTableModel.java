package tickets.ui;

import tickets.model.ResultRecord;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Таблица журнала выдачи: все записи results.xlsx в хронологическом порядке, повторы включены. */
final class HistoryTableModel extends AbstractTableModel {
    static final int TICKET_COLUMN = 3;
    static final int DATE_COLUMN = 4;
    static final int REPEAT_COLUMN = 5;

    private static final String[] COLUMNS = {"Группа", "Фамилия", "Имя", "Билет", "Дата и время", "Повтор"};
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final List<ResultRecord> records = new ArrayList<>();

    void setRecords(List<ResultRecord> newRecords) {
        records.clear();
        records.addAll(newRecords);
        fireTableDataChanged();
    }

    void add(ResultRecord record) {
        records.add(record);
        fireTableRowsInserted(records.size() - 1, records.size() - 1);
    }

    List<ResultRecord> records() {
        return Collections.unmodifiableList(records);
    }

    ResultRecord recordAt(int row) {
        return records.get(row);
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        ResultRecord r = records.get(row);
        return switch (column) {
            case 0 -> r.group();
            case 1 -> r.lastName();
            case 2 -> r.firstName();
            case TICKET_COLUMN -> String.valueOf(r.ticketNumber());
            case DATE_COLUMN -> r.dateTime() == null ? "" : r.dateTime().format(DATE);
            case REPEAT_COLUMN -> r.repeat() ? "да" : "нет";
            default -> throw new IllegalArgumentException("Нет колонки " + column);
        };
    }
}
