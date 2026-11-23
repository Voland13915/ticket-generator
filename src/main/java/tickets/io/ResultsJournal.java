package tickets.io;

import tickets.model.ResultRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** results.xlsx: колонки «Группа, Фамилия, Имя, Номер билета, Дата и время, Повтор (да/нет)». */
public final class ResultsJournal implements ResultsStore {
    static final List<String> HEADER =
            List.of("Группа", "Фамилия", "Имя", "Номер билета", "Дата и время", "Повтор (да/нет)");
    private static final String YES = "да";
    private static final String NO = "нет";

    private final XlsxLog log;

    public ResultsJournal(Path file) {
        this.log = new XlsxLog(file, "Журнал", HEADER);
    }

    @Override
    public List<ResultRecord> readAll() throws IOException {
        List<ResultRecord> records = new ArrayList<>();
        for (List<Object> row : log.readRows()) {
            ResultRecord record = toRecord(row);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    @Override
    public void append(ResultRecord record) throws IOException {
        log.append(List.of(record.group(), record.lastName(), record.firstName(),
                record.ticketNumber(), record.dateTime(), record.repeat() ? YES : NO));
    }

    /** Строки, где нет студента или номера билета (например, вписанные вручную), пропускаются. */
    private static ResultRecord toRecord(List<Object> row) {
        String group = text(row.get(0));
        String lastName = text(row.get(1));
        String firstName = text(row.get(2));
        if (group.isEmpty() || lastName.isEmpty() || firstName.isEmpty() || !(row.get(3) instanceof Double ticket)) {
            return null;
        }
        LocalDateTime dateTime = row.get(4) instanceof LocalDateTime date ? date : null;
        return new ResultRecord(group, lastName, firstName, ticket.intValue(), dateTime,
                YES.equalsIgnoreCase(text(row.get(5))));
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString().strip();
    }
}
