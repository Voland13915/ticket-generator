package tickets.console;

import java.io.IOException;

/** Куда консольный режим записывает выданные билеты. */
interface EntryLog {

    void append(Entry entry) throws IOException;
}
