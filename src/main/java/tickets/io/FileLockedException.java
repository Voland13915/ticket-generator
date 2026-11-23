package tickets.io;

import java.io.IOException;
import java.nio.file.Path;

/** Файл не удалось заменить — как правило, он открыт в Excel. */
public class FileLockedException extends IOException {

    public FileLockedException(Path file, IOException cause) {
        super("Файл " + file.getFileName() + " недоступен для записи (возможно, открыт в Excel): "
                + cause.getMessage(), cause);
    }
}
