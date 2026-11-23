package tickets.console;

import java.io.IOException;

/** Диалог с пользователем в консоли. Выделен интерфейсом, чтобы сценарий проверялся без терминала. */
interface Prompt {

    void println(String text);

    /** Запрашивает непустое значение. null — пользователь нажал ESC. */
    String readField(String label) throws IOException;

    /** Ждёт Enter (true) или ESC (false). */
    boolean awaitEnterOrEsc() throws IOException;
}
