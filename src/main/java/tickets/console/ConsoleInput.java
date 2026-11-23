package tickets.console;

import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Посимвольное чтение с консоли. Scanner.nextLine() не видит ESC,
 * поэтому терминал переводится в raw-режим и строка собирается вручную (Enter, Backspace, ESC).
 */
final class ConsoleInput implements Prompt, Closeable {
    private static final int ESC = 27;
    private static final int CTRL_C = 3;
    private static final int BACKSPACE = 8;
    private static final int DELETE = 127;
    /** Сколько ждать продолжения после ESC: стрелки и F-клавиши приходят как ESC + последовательность. */
    private static final long SEQUENCE_TIMEOUT_MS = 50;

    private final Terminal terminal;
    private final Attributes originalAttributes;
    private final NonBlockingReader reader;
    private final PrintWriter out;

    ConsoleInput() throws IOException {
        terminal = TerminalBuilder.builder().system(true).build();
        originalAttributes = terminal.enterRawMode();
        reader = terminal.reader();
        out = terminal.writer();

        if (terminal.getType().startsWith(Terminal.TYPE_DUMB)) {
            println("Внимание: консоль IDE не передаёт ESC. Запустите программу из cmd, PowerShell или терминала.");
        }
    }

    @Override
    public void println(String text) {
        out.println(text);
        out.flush();
    }

    /** Запрашивает непустое значение. Возвращает строку без пробелов по краям или null, если нажат ESC. */
    @Override
    public String readField(String label) throws IOException {
        while (true) {
            out.print(label);
            out.flush();

            String value = readLine();
            if (value == null) return null;

            value = value.strip();
            if (!value.isEmpty()) return value;
            println("Поле не может быть пустым, повторите ввод.");
        }
    }

    @Override
    public boolean awaitEnterOrEsc() throws IOException {
        while (true) {
            int key = readKey();
            if (isEnter(key)) {
                skipLineFeed(key);
                return true;
            }
            if (isExit(key)) return false;
        }
    }

    private String readLine() throws IOException {
        StringBuilder line = new StringBuilder();
        while (true) {
            int key = readKey();
            if (isExit(key)) {
                println("");
                return null;
            }
            if (isEnter(key)) {
                skipLineFeed(key);
                println("");
                return line.toString();
            }
            if (key == BACKSPACE || key == DELETE) {
                if (!line.isEmpty()) {
                    line.setLength(line.length() - 1);
                    out.print("\b \b");
                }
            } else if (key >= ' ') {
                line.append((char) key);
                out.print((char) key);
            }
            out.flush();
        }
    }

    /** Читает клавишу; управляющие последовательности (стрелки и т.п.) пропускает. */
    private int readKey() throws IOException {
        while (true) {
            int c = reader.read();
            if (c != ESC) return c;
            if (reader.peek(SEQUENCE_TIMEOUT_MS) < 0) return ESC; // за ESC ничего не пришло — это сама клавиша ESC
            skipEscapeSequence();
        }
    }

    /** Пропускает хвост последовательности вида ESC [ 1 ; 5 A или ESC O P. */
    private void skipEscapeSequence() throws IOException {
        int c = reader.read(SEQUENCE_TIMEOUT_MS);
        if (c != '[' && c != 'O') return;
        do {
            c = reader.read(SEQUENCE_TIMEOUT_MS);
        } while (c >= 0 && (c < '@' || c > '~'));
    }

    /** Некоторые терминалы шлют Enter как \r\n — съедаем \n, чтобы он не стал пустым вводом. */
    private void skipLineFeed(int key) throws IOException {
        if (key == '\r' && reader.peek(SEQUENCE_TIMEOUT_MS) == '\n') reader.read();
    }

    private static boolean isEnter(int key) {
        return key == '\r' || key == '\n';
    }

    /** ESC, Ctrl+C или конец ввода. */
    private static boolean isExit(int key) {
        return key == ESC || key == CTRL_C || key < 0;
    }

    @Override
    public void close() throws IOException {
        terminal.setAttributes(originalAttributes);
        terminal.close();
    }
}
