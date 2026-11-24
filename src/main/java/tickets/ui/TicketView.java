package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;
import tickets.model.Assignment;
import tickets.model.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Билет как бумажный экзаменационный билет: основная часть с вопросами и отрывной корешок
 * с номером. При повторной выдаче поверх ставится штамп.
 */
final class TicketView extends JPanel {
    private static final int STUB_WIDTH = 150;
    private static final int ARC = 20;
    private static final int NOTCH = 11;
    private static final int TEXT_WIDTH = 400;
    /** Поле вокруг бумаги под мягкую тень. */
    private static final int SHADOW = 10;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final boolean repeat;

    TicketView(Student student, Assignment assignment) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(SHADOW - 4, SHADOW, SHADOW + 4, SHADOW));
        this.repeat = assignment.record().repeat();
        add(main(student, assignment), BorderLayout.CENTER);
        add(stub(assignment), BorderLayout.EAST);
    }

    private static JComponent main(Student student, Assignment assignment) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(26, 32, 30, 28));

        JLabel caption = new JLabel("Экзаменационный билет");
        caption.putClientProperty(FlatClientProperties.STYLE_CLASS, "muted");
        JLabel title = new JLabel("Ваш билет № " + assignment.ticket().number());
        title.setFont(Theme.serif(Font.BOLD, 30f));
        title.putClientProperty(FlatClientProperties.STYLE_CLASS, "ink");
        JLabel who = new JLabel(student.displayName() + ", группа " + student.group());
        who.putClientProperty(FlatClientProperties.STYLE_CLASS, "large");

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 2, 0);
        panel.add(caption, c);
        panel.add(title, c);
        c.insets = new Insets(4, 0, 22, 0);
        panel.add(who, c);

        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        List<String> questions = assignment.ticket().questions();
        for (int i = 0; i < questions.size(); i++) {
            JLabel number = new JLabel((i + 1) + ".");
            number.setFont(Theme.serif(Font.BOLD, 18f));
            number.putClientProperty(FlatClientProperties.STYLE_CLASS, "ink");
            JLabel text = new JLabel("<html><body style='width: " + TEXT_WIDTH + "px'>"
                    + Html.escape(questions.get(i)) + "</body></html>");
            text.putClientProperty(FlatClientProperties.STYLE_CLASS, "large");

            c.gridy = 3 + i;
            c.gridx = 0;
            c.insets = new Insets(i == 0 ? 0 : 12, 0, 0, 12);
            panel.add(number, c);
            c.gridx = 1;
            c.insets = new Insets(i == 0 ? 3 : 15, 0, 0, 0);
            panel.add(text, c);
        }
        return panel;
    }

    private static JComponent stub(Assignment assignment) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(STUB_WIDTH, 10));

        JLabel caption = new JLabel("БИЛЕТ");
        caption.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "onPrimaryMuted"});
        JLabel number = new JLabel(String.valueOf(assignment.ticket().number()));
        number.setFont(Theme.serif(Font.BOLD, 64f));
        number.putClientProperty(FlatClientProperties.STYLE_CLASS, "onPrimary");
        JLabel date = new JLabel(assignment.record().dateTime() == null ? " "
                : assignment.record().dateTime().format(DATE));
        date.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "onPrimaryMuted"});

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        panel.add(caption, c);
        panel.add(number, c);
        panel.add(date, c);
        return panel;
    }

    /** Тень, бумага с вырезами по линии отрыва, цветной корешок и пунктир перфорации. */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        Insets in = getInsets();
        float left = in.left + 0.5f;
        float top = in.top + 0.5f;
        float w = getWidth() - in.left - in.right - 1f;
        float h = getHeight() - in.top - in.bottom - 1f;
        float x = left + w - STUB_WIDTH;

        Area notches = new Area(new Ellipse2D.Float(x - NOTCH, top - NOTCH, 2 * NOTCH, 2 * NOTCH));
        notches.add(new Area(new Ellipse2D.Float(x - NOTCH, top + h - NOTCH, 2 * NOTCH, 2 * NOTCH)));

        // тень не должна просвечивать сквозь вырезы
        Area outsideNotches = new Area(new Rectangle2D.Float(0, 0, getWidth(), getHeight()));
        outsideNotches.subtract(notches);
        Graphics2D shadow = (Graphics2D) g2.create();
        shadow.clip(outsideNotches);
        paintShadow(shadow, left, top, w, h);
        shadow.dispose();

        Area paper = new Area(new RoundRectangle2D.Float(left, top, w, h, ARC, ARC));
        paper.subtract(notches);
        g2.setColor(Theme.color("paper"));
        g2.fill(paper);

        Area stub = new Area(paper);
        stub.intersect(new Area(new Rectangle2D.Float(x, top, STUB_WIDTH + 1, h + 1)));
        g2.setColor(Theme.color("primary"));
        g2.fill(stub);

        g2.setColor(Theme.color("line"));
        g2.draw(paper);

        g2.setColor(Theme.color("onPrimaryMuted"));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{1f, 6f}, 0f));
        g2.draw(new Line2D.Float(x + 1, top + NOTCH + 8, x + 1, top + h - NOTCH - 8));
        g2.dispose();
    }

    /** Мягкая тень: несколько полупрозрачных слоёв, смещённых вниз. */
    private static void paintShadow(Graphics2D g2, float left, float top, float w, float h) {
        Color base = Theme.color("shadow");
        int layers = SHADOW - 2;
        for (int i = layers; i > 0; i--) {
            int alpha = Math.max(1, (int) (Theme.isDark() ? 30f / i : 14f / i));
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
            g2.fill(new RoundRectangle2D.Float(left - i, top - i + 4, w + 2 * i, h + 2 * i, ARC + 2 * i, ARC + 2 * i));
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (repeat) {
            paintStamp(g);
        }
    }

    /** Штамп «Повторная выдача» в правом верхнем углу основной части. */
    private void paintStamp(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        String top = "Повторная выдача";
        String bottom = "тот же билет";
        Font big = getFont().deriveFont(Font.BOLD, 14f);
        Font small = getFont().deriveFont(12f);
        FontMetrics fmBig = g2.getFontMetrics(big);
        FontMetrics fmSmall = g2.getFontMetrics(small);
        int bw = Math.max(fmBig.stringWidth(top), fmSmall.stringWidth(bottom)) + 30;
        int bh = fmBig.getHeight() + fmSmall.getHeight() + 14;
        int cx = getWidth() - getInsets().right - STUB_WIDTH - 26 - bw / 2;
        int cy = getInsets().top + 30 + bh / 2;

        Color base = Theme.color("stamp");
        g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 210));
        g2.rotate(Math.toRadians(-7), cx, cy);
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawRoundRect(cx - bw / 2, cy - bh / 2, bw, bh, 12, 12);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(cx - bw / 2 + 4, cy - bh / 2 + 4, bw - 8, bh - 8, 8, 8);

        int y = cy - bh / 2 + 7 + fmBig.getAscent();
        g2.setFont(big);
        g2.drawString(top, cx - fmBig.stringWidth(top) / 2, y);
        g2.setFont(small);
        g2.drawString(bottom, cx - fmSmall.stringWidth(bottom) / 2, y + fmSmall.getHeight());
        g2.dispose();
    }
}
