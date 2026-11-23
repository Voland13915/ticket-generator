package tickets.io;

import tickets.model.ResultRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Журнал в памяти для тестов. Умеет имитировать недоступный (открытый в Excel) файл. */
public final class InMemoryResultsStore implements ResultsStore {
    public final List<ResultRecord> records = new ArrayList<>();
    /** Сколько ближайших чтений упадёт. */
    public int readFailures;
    /** Сколько ближайших дозаписей упадёт. */
    public int appendFailures;

    @Override
    public List<ResultRecord> readAll() throws IOException {
        if (readFailures > 0) {
            readFailures--;
            throw new IOException("файл занят");
        }
        return List.copyOf(records);
    }

    @Override
    public void append(ResultRecord record) throws IOException {
        if (appendFailures > 0) {
            appendFailures--;
            throw new IOException("файл занят");
        }
        records.add(record);
    }
}
