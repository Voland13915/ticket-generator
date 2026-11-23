package tickets.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Безопасная запись файла: данные пишутся во временный файл, сбрасываются на диск
 * и только потом подменяют основной. Если процесс убьют посреди записи, старый файл останется целым.
 */
final class AtomicFile {

    @FunctionalInterface
    interface Writer {
        void write(OutputStream out) throws IOException;
    }

    private AtomicFile() {
    }

    /** @throws FileLockedException если основной файл заменить не удалось (обычно он открыт в Excel) */
    static void write(Path target, Writer writer) throws IOException {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            try (FileOutputStream out = new FileOutputStream(tmp.toFile())) {
                writer.write(out);
                out.getFD().sync();
            }
            replace(tmp, target);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private static void replace(Path tmp, Path target) throws IOException {
        try {
            try {
                Files.move(tmp, target, REPLACE_EXISTING, ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new FileLockedException(target, e);
        }
    }
}
