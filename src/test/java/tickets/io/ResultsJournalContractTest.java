package tickets.io;

import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

/** Настоящий results.xlsx подчиняется контракту ResultsStore. */
class ResultsJournalContractTest extends ResultsStoreContractTest {

    @TempDir
    Path dir;

    @Override
    protected ResultsStore createStore() {
        return new ResultsJournal(dir.resolve("results.xlsx"));
    }
}
