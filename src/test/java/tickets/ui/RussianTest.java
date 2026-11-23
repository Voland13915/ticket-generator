package tickets.ui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RussianTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0 групп", "1, 1 группа", "2, 2 группы", "4, 4 группы", "5, 5 групп",
            "11, 11 групп", "12, 12 групп", "14, 14 групп", "21, 21 группа", "22, 22 группы",
            "25, 25 групп", "101, 101 группа", "111, 111 групп", "112, 112 групп", "1004, 1004 группы"})
    void choosesWordFormByNumber(int n, String expected) {
        assertEquals(expected, Russian.count(n, "группа", "группы", "групп"));
    }
}
