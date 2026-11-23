package tickets.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tickets.model.Student;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SelectionPanelTest {
    private static final Student IVANOV = new Student("TI-237", "Иванов", "Иван");
    private static final Student PETROV = new Student("TI-237", "Петров", "Пётр");
    private static final Student SIDOROV = new Student("TI-238", "Сидоров", "Сидор");

    private SelectionPanel panel;

    @BeforeEach
    void setUp() {
        Map<String, List<Student>> groups = new LinkedHashMap<>();
        groups.put("TI-237", List.of(IVANOV, PETROV));
        groups.put("TI-238", List.of(SIDOROV));
        groups.put("TI-239", List.of());
        panel = new SelectionPanel();
        panel.setCatalog(groups);
    }

    private static List<Object> items(javax.swing.JComboBox<?> box) {
        List<Object> items = new ArrayList<>();
        for (int i = 0; i < box.getItemCount(); i++) {
            items.add(box.getItemAt(i));
        }
        return items;
    }

    @Test
    void groupListShowsSheetNamesAndNothingIsSelected() {
        assertEquals(List.of("TI-237", "TI-238", "TI-239"), items(panel.groupBox));
        assertNull(panel.groupBox.getSelectedItem());
        assertNull(panel.studentBox.getSelectedItem());
    }

    @Test
    void buttonIsDisabledBeforeAnySelection() {
        assertFalse(panel.generateButton.isEnabled());
        assertFalse(panel.studentBox.isEnabled());
    }

    @Test
    void choosingGroupFillsStudentsOfThatGroupButKeepsButtonDisabled() {
        panel.groupBox.setSelectedItem("TI-237");

        assertEquals(List.of(IVANOV, PETROV), items(panel.studentBox));
        assertNull(panel.studentBox.getSelectedItem());
        assertTrue(panel.studentBox.isEnabled());
        assertFalse(panel.generateButton.isEnabled());
    }

    @Test
    void buttonBecomesEnabledOnlyWhenBothAreChosen() {
        panel.groupBox.setSelectedItem("TI-237");
        panel.studentBox.setSelectedItem(PETROV);

        assertTrue(panel.generateButton.isEnabled());
    }

    @Test
    void changingGroupResetsStudentAndDisablesButton() {
        panel.groupBox.setSelectedItem("TI-237");
        panel.studentBox.setSelectedItem(PETROV);

        panel.groupBox.setSelectedItem("TI-238");

        assertEquals(List.of(SIDOROV), items(panel.studentBox));
        assertNull(panel.studentBox.getSelectedItem());
        assertFalse(panel.generateButton.isEnabled());
    }

    @Test
    void emptyGroupGivesEmptyListAndDisabledButtonWithoutException() {
        panel.groupBox.setSelectedItem("TI-239");

        assertEquals(0, panel.studentBox.getItemCount());
        assertFalse(panel.studentBox.isEnabled());
        assertFalse(panel.generateButton.isEnabled());
    }

    @Test
    void clickPassesSelectedStudentToListener() {
        List<Student> received = new ArrayList<>();
        panel.onGenerate(received::add);
        panel.groupBox.setSelectedItem("TI-237");
        panel.studentBox.setSelectedItem(IVANOV);

        panel.generateButton.doClick();

        assertEquals(List.of(IVANOV), received);
    }

    @Test
    void disabledButtonDoesNotFireListener() {
        List<Student> received = new ArrayList<>();
        panel.onGenerate(received::add);
        panel.groupBox.setSelectedItem("TI-237");

        panel.generateButton.doClick();

        assertTrue(received.isEmpty());
    }

    @Test
    void resetStudentKeepsGroupAndDisablesButton() {
        panel.groupBox.setSelectedItem("TI-237");
        panel.studentBox.setSelectedItem(IVANOV);

        panel.resetStudent();

        assertEquals("TI-237", panel.groupBox.getSelectedItem());
        assertNull(panel.studentBox.getSelectedItem());
        assertFalse(panel.generateButton.isEnabled());
    }

    @Test
    void emptyCatalogDisablesEverything() {
        panel.setCatalog(Map.of());

        assertEquals(0, panel.groupBox.getItemCount());
        assertFalse(panel.groupBox.isEnabled());
        assertFalse(panel.generateButton.isEnabled());
    }
}
