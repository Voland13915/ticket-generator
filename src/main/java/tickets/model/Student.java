package tickets.model;

import java.util.Locale;

/** Студент конкретной группы (группа = имя листа в students.xlsx). */
public record Student(String group, String lastName, String firstName) {

    public String displayName() {
        return lastName + " " + firstName;
    }

    /** Ключ для сравнения: регистр и лишние пробелы не важны. */
    public String key() {
        return key(group, lastName, firstName);
    }

    public static String key(String group, String lastName, String firstName) {
        return normalize(group) + "|" + normalize(lastName) + "|" + normalize(firstName);
    }

    private static String normalize(String text) {
        return text.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Нужен выпадающему списку: он показывает toString(). */
    @Override
    public String toString() {
        return displayName();
    }
}
