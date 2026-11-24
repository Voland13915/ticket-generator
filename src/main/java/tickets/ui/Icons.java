package tickets.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Векторные иконки интерфейса. Рисуются кодом на сетке 16×16 и масштабируются,
 * поэтому чёткие на любом экране и не требуют файлов картинок. Цвет берётся из темы
 * в момент отрисовки — после переключения темы иконки перекрашиваются сами.
 */
final class Icons {

    enum Kind { REFRESH, MOON, SUN, WARNING, ERROR, TICKET, CHECK }

    private Icons() {
    }

    /**
     * @param color имя цвета из {@link Theme#color}; null — цвет текста компонента
     *              (для отключённой кнопки — цвет отключённого текста)
     */
    static Icon of(Kind kind, int size, String color) {
        return new VectorIcon(kind, size, color);
    }

    /** Значок приложения для панели задач: билет на синем фоне, несколько размеров. */
    static List<Image> appImages() {
        List<Image> images = new ArrayList<>();
        for (int size : new int[]{16, 24, 32, 48, 64, 128, 256}) {
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = Theme.smooth(image.getGraphics());
            paintBrand(g2, 0, 0, size, new Color(0x1F4E79), Color.WHITE);
            g2.dispose();
            images.add(image);
        }
        return images;
    }

    /** Фирменный знак: скруглённый квадрат с билетом внутри. */
    static void paintBrand(Graphics2D g2, int x, int y, int size, Color background, Color foreground) {
        g2.setColor(background);
        g2.fill(new RoundRectangle2D.Float(x, y, size, size, size * 0.42f, size * 0.42f));
        Graphics2D inner = (Graphics2D) g2.create();
        inner.translate(x + size * 0.18, y + size * 0.18);
        inner.scale(size * 0.64 / 16.0, size * 0.64 / 16.0);
        inner.setColor(foreground);
        inner.fill(ticket());
        inner.dispose();
    }

    /** Билет с полукруглыми вырезами по бокам. */
    private static Shape ticket() {
        Area area = new Area(new RoundRectangle2D.Float(0.5f, 3f, 15f, 10f, 3f, 3f));
        area.subtract(new Area(new Ellipse2D.Float(-1.6f, 5.9f, 4.2f, 4.2f)));
        area.subtract(new Area(new Ellipse2D.Float(13.4f, 5.9f, 4.2f, 4.2f)));
        // перфорация корешка
        for (int i = 0; i < 3; i++) {
            area.subtract(new Area(new Ellipse2D.Float(10.6f, 4.6f + i * 2.6f, 1.2f, 1.2f)));
        }
        return area;
    }

    private static final class VectorIcon implements Icon {
        private final Kind kind;
        private final int size;
        private final String color;

        VectorIcon(Kind kind, int size, String color) {
            this.kind = kind;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = Theme.smooth(g);
            g2.translate(x, y);
            g2.scale(size / 16.0, size / 16.0);
            g2.setColor(colorFor(c));
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            paint(g2);
            g2.dispose();
        }

        private Color colorFor(Component c) {
            if (c != null && !c.isEnabled()) {
                return Theme.color("disabledText");
            }
            if (color != null) {
                return Theme.color(color);
            }
            return c != null ? c.getForeground() : Theme.color("ink");
        }

        private void paint(Graphics2D g2) {
            switch (kind) {
                case REFRESH -> {
                    g2.draw(new Arc2D.Float(2.5f, 2.5f, 11f, 11f, 70, 290, Arc2D.OPEN));
                    Path2D arrow = new Path2D.Float();
                    arrow.moveTo(13.9, 4.6);
                    arrow.lineTo(13.6, 8.4);
                    arrow.lineTo(9.9, 7.4);
                    arrow.closePath();
                    g2.fill(arrow);
                    g2.draw(arrow);
                }
                case MOON -> {
                    Area moon = new Area(new Ellipse2D.Float(2f, 2f, 12f, 12f));
                    moon.subtract(new Area(new Ellipse2D.Float(5.5f, -0.5f, 11f, 11f)));
                    g2.fill(moon);
                }
                case SUN -> {
                    g2.fill(new Ellipse2D.Float(5f, 5f, 6f, 6f));
                    for (int i = 0; i < 8; i++) {
                        double a = Math.toRadians(i * 45);
                        g2.draw(new Line2D.Double(8 + 5 * Math.cos(a), 8 + 5 * Math.sin(a),
                                8 + 6.8 * Math.cos(a), 8 + 6.8 * Math.sin(a)));
                    }
                }
                case WARNING -> {
                    Path2D triangle = new Path2D.Float();
                    triangle.moveTo(8, 2);
                    triangle.lineTo(14.6, 13.5);
                    triangle.lineTo(1.4, 13.5);
                    triangle.closePath();
                    g2.draw(triangle);
                    exclamation(g2, 6.2f, 9.2f, 11.6f);
                }
                case ERROR -> {
                    g2.draw(new Ellipse2D.Float(1.8f, 1.8f, 12.4f, 12.4f));
                    exclamation(g2, 4.8f, 8.6f, 11.2f);
                }
                case TICKET -> g2.fill(ticket());
                case CHECK -> {
                    Path2D check = new Path2D.Float();
                    check.moveTo(3.2, 8.4);
                    check.lineTo(6.6, 11.6);
                    check.lineTo(12.8, 4.6);
                    g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(check);
                }
            }
        }

        private static void exclamation(Graphics2D g2, float top, float bottom, float dot) {
            g2.draw(new Line2D.Float(8f, top, 8f, bottom));
            g2.fill(new Ellipse2D.Float(7.1f, dot - 0.9f, 1.8f, 1.8f));
        }
    }
}
