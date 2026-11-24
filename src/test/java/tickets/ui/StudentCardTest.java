package tickets.ui;

import org.junit.jupiter.api.Test;
import tickets.model.Student;

import static org.junit.jupiter.api.Assertions.*;

class StudentCardTest {

    @Test
    void initialsAreLastNameThenFirstName() {
        assertEquals("ИИ", StudentCard.initials(new Student("TI-237", "Иванов", "Иван")));
    }

    @Test
    void initialsIgnoreSpacesAndCase() {
        assertEquals("ПП", StudentCard.initials(new Student("TI-237", "  петров", "пётр ")));
    }

    @Test
    void cardIsHiddenUntilStudentIsChosen() {
        StudentCard card = new StudentCard();
        assertFalse(card.isVisible());

        card.show(new Student("TI-237", "Иванов", "Иван"));
        assertTrue(card.isVisible());

        card.show(null);
        assertFalse(card.isVisible());
    }
}
