package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;
import tickets.model.Student;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Боковая панель: выбор группы и студента. Кнопка «Сгенерировать билет» активна только когда выбраны оба.
 * Логика не зависит от окна, поэтому проверяется тестами без экрана.
 */
final class SelectionPanel extends JPanel {
    static final int WIDTH = 300;
    private static final String EMPTY_GROUP_HINT = "В этой группе нет студентов. Добавьте их на лист в students.xlsx.";

    private final DefaultComboBoxModel<String> groupModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<Student> studentModel = new DefaultComboBoxModel<>();
    final JComboBox<String> groupBox = new JComboBox<>(groupModel);
    final JComboBox<Student> studentBox = new JComboBox<>(studentModel);
    final JButton generateButton = new JButton("Сгенерировать билет");
    private final WrappingText hint = new WrappingText("errorText");

    private Map<String, List<Student>> groups = Map.of();
    /** true, пока списки заполняются программно: события выбора в это время игнорируются. */
    private boolean adjusting;
    private Consumer<Student> generateListener = student -> { };

    SelectionPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(22, 24, 20, 24));
        groupBox.setRenderer(new PlaceholderRenderer("Выберите группу"));
        studentBox.setRenderer(new PlaceholderRenderer("Выберите студента"));
        generateButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "primary");

        layoutComponents();

        groupBox.addActionListener(e -> {
            if (!adjusting) {
                fillStudents(selectedGroup());
            }
        });
        studentBox.addActionListener(e -> {
            if (!adjusting) {
                updateState();
            }
        });
        generateButton.addActionListener(e -> {
            Student student = selectedStudent();
            if (student != null) {
                generateListener.accept(student);
            }
        });
        setCatalog(Map.of());
    }

    void onGenerate(Consumer<Student> listener) {
        this.generateListener = listener;
    }

    /** Заменяет список групп; выбор сбрасывается. */
    void setCatalog(Map<String, List<Student>> newGroups) {
        adjusting = true;
        try {
            groups = newGroups;
            groupModel.removeAllElements();
            groupModel.addAll(newGroups.keySet());
            groupModel.setSelectedItem(null);
            groupBox.setEnabled(!newGroups.isEmpty());
        } finally {
            adjusting = false;
        }
        fillStudents(null);
    }

    /** Сбрасывает выбор студента (группа остаётся) — готовность к следующему студенту. */
    void resetStudent() {
        studentModel.setSelectedItem(null);
        studentBox.requestFocusInWindow();
    }

    private void fillStudents(String group) {
        List<Student> students = group == null ? List.of() : groups.getOrDefault(group, List.of());
        adjusting = true;
        try {
            studentModel.removeAllElements();
            studentModel.addAll(students);
            studentModel.setSelectedItem(null);
            studentBox.setEnabled(!students.isEmpty());
        } finally {
            adjusting = false;
        }
        updateState();
    }

    private void updateState() {
        boolean emptyGroup = selectedGroup() != null && studentModel.getSize() == 0;
        hint.setText(emptyGroup ? EMPTY_GROUP_HINT : "");
        generateButton.setEnabled(selectedGroup() != null && selectedStudent() != null);
    }

    private String selectedGroup() {
        return (String) groupBox.getSelectedItem();
    }

    private Student selectedStudent() {
        return (Student) studentBox.getSelectedItem();
    }

    /** Фон боковой панели и линия справа. */
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Theme.color("sidebar"));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Theme.color("line"));
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, super.getPreferredSize().height);
    }

    private void layoutComponents() {
        JLabel title = new JLabel("Кому выдать билет");
        title.putClientProperty(FlatClientProperties.STYLE_CLASS, "h3");
        JLabel lead = new JLabel("Выберите группу, затем студента.");
        lead.putClientProperty(FlatClientProperties.STYLE_CLASS, "muted");

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.insets = new Insets(0, 0, 4, 0);
        add(title, c);
        c.insets = new Insets(0, 0, 20, 0);
        add(lead, c);

        addField("Группа", groupBox, c);
        addField("Студент", studentBox, c);

        c.insets = new Insets(0, 0, 14, 0);
        add(hint, c);

        c.ipady = 12;
        c.insets = new Insets(0, 0, 0, 0);
        add(generateButton, c);

        c.ipady = 0;
        c.weighty = 1;
        add(Box.createGlue(), c);

        c.weighty = 0;
        add(shortcuts(), c);
    }

    private void addField(String label, JComponent field, GridBagConstraints c) {
        JLabel caption = new JLabel(label);
        caption.putClientProperty(FlatClientProperties.STYLE_CLASS, "semibold");
        caption.setLabelFor(field);
        c.insets = new Insets(0, 0, 6, 0);
        add(caption, c);
        c.ipady = 6;
        c.insets = new Insets(0, 0, 14, 0);
        add(field, c);
        c.ipady = 0;
    }

    private static JPanel shortcuts() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        String[][] rows = {
                {"Enter", "сгенерировать билет"},
                {"Esc", "закрыть окно билета"},
                {"F5", "перечитать файлы"}};
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 10);
        for (int i = 0; i < rows.length; i++) {
            Pill key = new Pill(rows[i][0]);
            key.setColors("paper", "line");
            key.putClientProperty(FlatClientProperties.STYLE_CLASS, "small");
            JLabel action = new JLabel(rows[i][1]);
            action.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "muted"});

            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            panel.add(key, c);
            c.gridx = 1;
            c.weightx = 1;
            panel.add(action, c);
        }
        return panel;
    }

    /** Показывает подсказку приглушённым цветом, пока в списке ничего не выбрано. */
    private static final class PlaceholderRenderer extends DefaultListCellRenderer {
        private final String placeholder;

        PlaceholderRenderer(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                label.setText(placeholder);
                label.setForeground(Theme.color("muted"));
            }
            return label;
        }
    }
}
