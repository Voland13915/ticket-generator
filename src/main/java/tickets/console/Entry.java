package tickets.console;

import java.time.LocalDateTime;

/** Одна строка консольного журнала. */
record Entry(String lastName, String firstName, int ticket, LocalDateTime dateTime) {
}
