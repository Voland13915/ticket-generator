package tickets.model;

import java.time.LocalDateTime;

/** Одна строка results.xlsx. */
public record ResultRecord(String group, String lastName, String firstName,
                           int ticketNumber, LocalDateTime dateTime, boolean repeat) {

    public String studentKey() {
        return Student.key(group, lastName, firstName);
    }
}
