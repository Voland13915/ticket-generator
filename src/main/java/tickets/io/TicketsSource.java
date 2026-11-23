package tickets.io;

import java.io.IOException;

/** Откуда берутся билеты. Некорректные билеты источник пропускает и перечисляет в предупреждениях. */
public interface TicketsSource {

    String name();

    TicketsParser.Result read() throws IOException;
}
