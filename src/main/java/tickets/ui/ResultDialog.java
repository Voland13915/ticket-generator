package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;
import tickets.model.Assignment;
import tickets.model.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/** Окно результата с билетом. ESC закрывает окно, а не приложение. */
final class ResultDialog extends JDialog {

    ResultDialog(Frame owner, Student student, Assignment assignment) {
        super(owner, "Билет № " + assignment.ticket().number(), true);

        JButton done = new JButton("Готово");
        done.putClientProperty(FlatClientProperties.STYLE_CLASS, "primary");
        done.setIcon(Icons.of(Icons.Kind.CHECK, 16, "onPrimary"));
        done.setMargin(new Insets(6, 18, 6, 22));
        done.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(done);
        Keys.bind(getRootPane(), KeyEvent.VK_ESCAPE, this::dispose);

        JLabel hint = new JLabel("Esc или «Готово» вернут к выбору студента");
        hint.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "muted"});
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 10)); // по краю бумаги, а не тени
        footer.add(hint, BorderLayout.WEST);
        footer.add(done, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 18, 16));
        content.add(new TicketView(student, assignment), BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);

        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }
}
