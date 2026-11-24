package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Панель с ошибками загрузки и кнопкой «Повторить загрузку». Скрыта, пока ошибок нет. */
final class ErrorBanner extends JPanel {
    private static final int MARGIN = 18;
    private static final int PADDING = 16;

    /** Не надпись, а переносимый текст: длинные пути к файлам переносятся по ширине окна. */
    private final WrappingText details = new WrappingText("errorText");
    private final JButton retry = new JButton("Повторить загрузку");

    ErrorBanner() {
        super(new BorderLayout(20, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(MARGIN + PADDING, MARGIN + PADDING, PADDING, MARGIN + PADDING));

        JLabel title = new JLabel("Не удалось загрузить данные", Icons.of(Icons.Kind.ERROR, 18, "errorText"),
                SwingConstants.LEADING);
        title.setIconTextGap(10);
        title.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"semibold", "errorText"});
        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        // текст ошибки выровнен по заголовку, а не по значку
        details.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 0));
        text.add(details, BorderLayout.CENTER);
        retry.setIcon(Icons.of(Icons.Kind.REFRESH, 16, "errorText"));

        JPanel buttonHolder = new JPanel(new GridBagLayout());
        buttonHolder.setOpaque(false);
        buttonHolder.add(retry);

        add(text, BorderLayout.CENTER);
        add(buttonHolder, BorderLayout.EAST);
        setVisible(false);
    }

    void onRetry(Runnable action) {
        retry.addActionListener(e -> action.run());
    }

    void showErrors(List<String> errors) {
        details.setText(String.join("\n", errors));
        setVisible(true);
    }

    void clear() {
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        int w = getWidth() - 2 * MARGIN - 1;
        int h = getHeight() - MARGIN - 1;
        g2.setColor(Theme.color("errorBackground"));
        g2.fillRoundRect(MARGIN, MARGIN, w, h, 16, 16);
        g2.setColor(Theme.color("errorBorder"));
        g2.drawRoundRect(MARGIN, MARGIN, w, h, 16, 16);
        g2.dispose();
    }
}
