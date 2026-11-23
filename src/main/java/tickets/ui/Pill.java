package tickets.ui;

import javax.swing.*;
import java.awt.*;

/** Подпись на скруглённой подложке: бейдж «да» в журнале и клавиши в подсказках. */
final class Pill extends JLabel {
    private String fill;
    private String border;

    Pill(String text) {
        super(text);
        setBorder(BorderFactory.createEmptyBorder(2, 9, 2, 9));
    }

    /** Имена цветов из {@link Theme#color}; null — без заливки или без рамки. */
    void setColors(String fill, String border) {
        this.fill = fill;
        this.border = border;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (fill != null || border != null) {
            Graphics2D g2 = Theme.smooth(g);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            if (fill != null) {
                g2.setColor(Theme.color(fill));
                g2.fillRoundRect(0, 0, w, h, h, h);
            }
            if (border != null) {
                g2.setColor(Theme.color(border));
                g2.drawRoundRect(0, 0, w, h, 10, 10);
            }
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
