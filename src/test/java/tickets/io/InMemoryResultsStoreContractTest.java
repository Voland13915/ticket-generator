package tickets.io;

/** Фейк из тестов обязан подчиняться тому же контракту, что и настоящий журнал. */
class InMemoryResultsStoreContractTest extends ResultsStoreContractTest {

    @Override
    protected ResultsStore createStore() {
        return new InMemoryResultsStore();
    }
}
