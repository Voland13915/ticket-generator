package tickets.service;

import tickets.model.Student;
import tickets.model.Ticket;

import java.util.List;
import java.util.Map;

/** Всё, что прочитано из входных файлов: группы со студентами и корректные билеты. */
public record Catalog(Map<String, List<Student>> groups, List<Ticket> tickets) {
}
