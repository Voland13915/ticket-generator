package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

/** Строка состояния: цветная точка (всё хорошо / внимание / ошибка), сообщение и имя файла журнала. */
final class StatusBar extends JPanel {

    /** Оттенок сообщения: определяет цвет точки. */
    enum Tone {
        OK("success"), INFO("muted"), WARNING("repeatText"), ERROR("errorText");

        private final String color;

        Tone(String color) {
            this.color = color;
        }
    }

    private final JLabel message = new JLabel(" ");
    private Tone tone = Tone.INFO;

    StatusBar(String journalName) {
        super(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        message.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "muted"});
        message.setIcon(new Dot());
        message.setIconTextGap(8);

        JLabel journal = new JLabel("Журнал: " + journalName);
        journal.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "muted"});

        add(message, BorderLayout.CENTER);
        add(journal, BorderLayout.EAST);
    }

    void show(String text, Tone tone) {
        this.tone = tone;
        message.setText(text);
        message.repaint();
    }

    String text() {
        return message.getText();
    }

    Tone tone() {
        return tone;
    }

    /** Тонкая линия сверху отделяет строку от содержимого окна. */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Theme.color("line"));
        g.drawLine(0, 0, getWidth(), 0);
    }

    private final class Dot implements Icon {
        private static final int SIZE = 8;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = Theme.smooth(g);
            g2.setColor(Theme.color(tone.color));
            g2.fillOval(x, y, SIZE, SIZE);
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
}
