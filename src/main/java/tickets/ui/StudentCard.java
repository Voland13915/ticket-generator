package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;
import tickets.model.Student;

import javax.swing.*;
import java.awt.*;

/** Карточка выбранного студента над кнопкой: кружок с инициалами, имя и группа. Скрыта, пока никто не выбран. */
final class StudentCard extends JPanel {
    private static final int ARC = 14;

    private final Avatar avatar = new Avatar();
    private final JLabel name = new JLabel();
    private final JLabel group = new JLabel();

    StudentCard() {
        super(new BorderLayout(12, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        name.putClientProperty(FlatClientProperties.STYLE_CLASS, "semibold");
        group.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "muted"});

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
        text.setOpaque(false);
        text.add(name);
        text.add(group);

        add(avatar, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
        setVisible(false);
    }

    void show(Student student) {
        setVisible(student != null);
        if (student == null) {
            return;
        }
        avatar.initials = initials(student);
        name.setText(student.displayName());
        group.setText("группа " + student.group());
        revalidate();
        repaint();
    }

    /** «Иванов Иван» → «ИИ». */
    static String initials(Student student) {
        return firstLetter(student.lastName()) + firstLetter(student.firstName());
    }

    private static String firstLetter(String word) {
        String stripped = word.strip();
        return stripped.isEmpty() ? "" : stripped.substring(0, 1).toUpperCase();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        g2.setColor(Theme.color("paper"));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        g2.setColor(Theme.color("line"));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        g2.dispose();
    }

    private static final class Avatar extends JComponent {
        private static final int SIZE = 36;
        private String initials = "";

        Avatar() {
            setPreferredSize(new Dimension(SIZE, SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.smooth(g);
            int y = (getHeight() - SIZE) / 2;
            g2.setColor(Theme.color("badge"));
            g2.fillOval(0, y, SIZE, SIZE);
            g2.setColor(Theme.color("ink"));
            g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initials, (SIZE - fm.stringWidth(initials)) / 2f, y + (SIZE - fm.getHeight()) / 2f + fm.getAscent());
            g2.dispose();
        }
    }
}
