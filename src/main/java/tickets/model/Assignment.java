package tickets.model;

/** Итог выдачи: билет для показа и строка, записанная в журнал. */
public record Assignment(Ticket ticket, ResultRecord record) {
}
