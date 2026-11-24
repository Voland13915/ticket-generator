package tickets.io;

import tickets.model.Ticket;

import java.util.List;

/**
 * Результат чтения билетов: корректные билеты и предупреждения о пропущенных.
 * Отдельный тип, а не вложенный класс парсера: интерфейс {@link TicketsSource}
 * не должен зависеть от конкретного способа разбора (принцип инверсии зависимостей).
 */
public record ParsedTickets(List<Ticket> tickets, List<String> warnings) {

    public ParsedTickets {
        tickets = List.copyOf(tickets);
        warnings = List.copyOf(warnings);
    }
}
