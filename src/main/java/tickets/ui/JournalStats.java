package tickets.ui;

import tickets.model.ResultRecord;

import java.util.List;

/** Сводка над журналом: сколько выдач, сколько разных студентов, сколько повторов. */
record JournalStats(int issued, int students, int repeats) {

    static JournalStats of(List<ResultRecord> records) {
        int students = (int) records.stream().map(ResultRecord::studentKey).distinct().count();
        int repeats = (int) records.stream().filter(ResultRecord::repeat).count();
        return new JournalStats(records.size(), students, repeats);
    }

    String describe() {
        if (issued == 0) {
            return "Записей пока нет";
        }
        return Russian.count(issued, "выдача", "выдачи", "выдач") + ", "
                + Russian.count(students, "студент", "студента", "студентов") + ", "
                + Russian.count(repeats, "повтор", "повтора", "повторов");
    }
}
