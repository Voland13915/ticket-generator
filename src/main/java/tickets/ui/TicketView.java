package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;
import tickets.model.Assignment;
import tickets.model.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
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
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final boolean repeat;

    TicketView(Student student, Assignment assignment) {
        super(new BorderLayout());
        setOpaque(false);
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

        JLabel caption = new JLabel("билет");
        caption.putClientProperty(FlatClientProperties.STYLE_CLASS, "muted");
        JLabel number = new JLabel(String.valueOf(assignment.ticket().number()));
        number.setFont(Theme.serif(Font.BOLD, 64f));
        number.putClientProperty(FlatClientProperties.STYLE_CLASS, "ink");
        JLabel date = new JLabel(assignment.record().dateTime() == null ? " "
                : assignment.record().dateTime().format(DATE));
        date.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "muted"});

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        panel.add(caption, c);
        panel.add(number, c);
        panel.add(date, c);
        return panel;
    }

    /** Бумага с вырезами по линии отрыва и пунктир перфорации. */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        int w = getWidth();
        int h = getHeight();
        int x = w - STUB_WIDTH;

        Area paper = new Area(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, ARC, ARC));
        paper.subtract(new Area(new Ellipse2D.Float(x - NOTCH, -NOTCH, 2 * NOTCH, 2 * NOTCH)));
        paper.subtract(new Area(new Ellipse2D.Float(x - NOTCH, h - NOTCH, 2 * NOTCH, 2 * NOTCH)));
        g2.setColor(Theme.color("paper"));
        g2.fill(paper);
        g2.setColor(Theme.color("line"));
        g2.draw(paper);

        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{1f, 6f}, 0f));
        g2.drawLine(x, NOTCH + 8, x, h - NOTCH - 8);
        g2.dispose();
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
        int cx = getWidth() - STUB_WIDTH - 26 - bw / 2;
        int cy = 30 + bh / 2;

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
