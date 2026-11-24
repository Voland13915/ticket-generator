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
    static final int WIDTH = 320;
    private static final String EMPTY_GROUP_HINT = "В этой группе нет студентов. Добавьте их на лист в students.xlsx.";

    private final DefaultComboBoxModel<String> groupModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<Student> studentModel = new DefaultComboBoxModel<>();
    final JComboBox<String> groupBox = new JComboBox<>(groupModel);
    final JComboBox<Student> studentBox = new JComboBox<>(studentModel);
    final JButton generateButton = new JButton("Сгенерировать билет");
    private final WrappingText hint = new WrappingText("errorText");
    private final StudentCard card = new StudentCard();
    private final StepIcon groupStep = new StepIcon(1);
    private final StepIcon studentStep = new StepIcon(2);

    private Map<String, List<Student>> groups = Map.of();
    /** true, пока списки заполняются программно: события выбора в это время игнорируются. */
    private boolean adjusting;
    private Consumer<Student> generateListener = student -> { };

    SelectionPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));
        groupBox.setRenderer(new GroupRenderer());
        studentBox.setRenderer(new PlaceholderRenderer("Выберите студента"));
        generateButton.putClientProperty(FlatClientProperties.STYLE_CLASS, "primary");
        Icon ticket = Icons.of(Icons.Kind.TICKET, 18, "onPrimary");
        generateButton.setIcon(ticket);
        generateButton.setDisabledIcon(ticket); // иконка сама берёт цвет отключённой кнопки
        generateButton.setIconTextGap(10);

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
        hint.setVisible(emptyGroup);
        generateButton.setEnabled(selectedGroup() != null && selectedStudent() != null);

        groupStep.done = selectedGroup() != null;
        studentStep.done = selectedStudent() != null;
        card.show(selectedStudent());
        repaint();
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
        c.insets = new Insets(0, 0, 22, 0);
        add(lead, c);

        addField(groupStep, "Группа", groupBox, c);
        addField(studentStep, "Студент", studentBox, c);

        c.insets = new Insets(0, 0, 14, 0);
        add(hint, c);
        add(card, c);

        c.ipady = 14;
        c.insets = new Insets(4, 0, 0, 0);
        add(generateButton, c);

        c.ipady = 0;
        c.weighty = 1;
        add(Box.createGlue(), c);

        c.weighty = 0;
        add(shortcuts(), c);
    }

    private void addField(StepIcon step, String label, JComponent field, GridBagConstraints c) {
        JLabel caption = new JLabel(label, step, SwingConstants.LEADING);
        caption.setIconTextGap(9);
        caption.putClientProperty(FlatClientProperties.STYLE_CLASS, "semibold");
        caption.setLabelFor(field);
        c.insets = new Insets(0, 0, 8, 0);
        add(caption, c);
        c.ipady = 6;
        c.insets = new Insets(0, 0, 18, 0);
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
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 0, 3, 10);
        for (int i = 0; i < rows.length; i++) {
            Pill key = new Pill(rows[i][0]);
            key.setColors("paper", "line");
            key.setHorizontalAlignment(SwingConstants.CENTER);
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

    /** Кружок с номером шага; когда шаг выполнен — залитый кружок с галочкой. */
    private static final class StepIcon implements Icon {
        private static final int SIZE = 20;
        private final int number;
        private boolean done;

        StepIcon(int number) {
            this.number = number;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = Theme.smooth(g);
            if (done) {
                g2.setColor(Theme.color("primary"));
                g2.fillOval(x, y, SIZE, SIZE);
                Icons.of(Icons.Kind.CHECK, 14, "onPrimary").paintIcon(null, g2, x + 3, y + 3);
            } else {
                g2.setColor(Theme.color("muted"));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawOval(x, y, SIZE - 1, SIZE - 1);
                g2.setFont(c.getFont().deriveFont(Font.BOLD, 11f));
                FontMetrics fm = g2.getFontMetrics();
                String text = String.valueOf(number);
                g2.drawString(text, x + (SIZE - fm.stringWidth(text)) / 2f,
                        y + (SIZE - fm.getHeight()) / 2f + fm.getAscent());
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }

    /** Группа и число студентов в ней; пока ничего не выбрано — подсказка. */
    private final class GroupRenderer implements ListCellRenderer<String> {
        private final PlaceholderRenderer placeholder = new PlaceholderRenderer("Выберите группу");
        private final JPanel row = new JPanel(new BorderLayout(12, 0));
        private final JLabel name = new JLabel();
        private final JLabel count = new JLabel();

        GroupRenderer() {
            row.add(name, BorderLayout.CENTER);
            row.add(count, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String group, int index,
                                                      boolean selected, boolean focus) {
            JLabel base = (JLabel) placeholder.getListCellRendererComponent(list, group, index, selected, focus);
            if (group == null) {
                return base;
            }
            row.setOpaque(base.isOpaque());
            row.setBackground(base.getBackground());
            row.setBorder(base.getBorder());
            name.setFont(base.getFont());
            name.setForeground(base.getForeground());
            name.setText(group);
            count.setFont(base.getFont().deriveFont(base.getFont().getSize2D() - 1f));
            count.setForeground(selected && index >= 0 ? base.getForeground() : Theme.color("muted"));
            int size = groups.getOrDefault(group, List.of()).size();
            count.setText(size == 0 ? "нет студентов" : Russian.count(size, "студент", "студента", "студентов"));
            return row;
        }
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
