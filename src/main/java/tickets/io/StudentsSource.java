package tickets.io;

import tickets.model.Student;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Откуда берётся список студентов по группам. Реализация может читать Excel, CSV, базу и т.д. */
public interface StudentsSource {

    /** Имя источника для сообщений пользователю (например, имя файла). */
    String name();

    /** Группы в нужном порядке; у группы без студентов — пустой список. */
    Map<String, List<Student>> read() throws IOException;
}
