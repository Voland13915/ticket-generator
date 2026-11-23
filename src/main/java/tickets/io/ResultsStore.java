package tickets.io;

import tickets.model.ResultRecord;

import java.io.IOException;
import java.util.List;

/** Хранилище журнала выдачи. Выделено интерфейсом, чтобы логику можно было проверять без Excel. */
public interface ResultsStore {

    /** Все записи в хронологическом порядке (в порядке строк файла). */
    List<ResultRecord> readAll() throws IOException;

    void append(ResultRecord record) throws IOException;
}
