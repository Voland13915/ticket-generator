package tickets.io;

import org.apache.poi.ss.usermodel.*;
import tickets.model.Student;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** students.xlsx: лист = группа, колонка A — фамилия, B — имя, заголовок в строке 1. */
public final class XlsxStudentsSource implements StudentsSource {
    private final Path file;

    public XlsxStudentsSource(Path file) {
        this.file = file;
    }

    @Override
    public String name() {
        return file.getFileName().toString();
    }

    @Override
    public Map<String, List<Student>> read() throws IOException {
        DataFormatter formatter = new DataFormatter();
        Map<String, List<Student>> groups = new LinkedHashMap<>();
        try (Workbook wb = ExcelFiles.open(file)) {
            for (Sheet sheet : wb) {
                List<Student> students = new ArrayList<>();
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) {
                        continue;
                    }
                    String lastName = formatter.formatCellValue(row.getCell(0)).strip();
                    String firstName = formatter.formatCellValue(row.getCell(1)).strip();
                    if (!lastName.isEmpty() && !firstName.isEmpty()) {
                        students.add(new Student(sheet.getSheetName(), lastName, firstName));
                    }
                }
                groups.put(sheet.getSheetName(), List.copyOf(students));
            }
        }
        return groups;
    }
}
