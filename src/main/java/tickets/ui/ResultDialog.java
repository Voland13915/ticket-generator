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
        done.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(done);
        Keys.bind(getRootPane(), KeyEvent.VK_ESCAPE, this::dispose);

        JLabel hint = new JLabel("Esc или «Готово» вернут к выбору студента");
        hint.putClientProperty(FlatClientProperties.STYLE_CLASS, new String[]{"small", "muted"});
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(hint, BorderLayout.WEST);
        footer.add(done, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(BorderFactory.createEmptyBorder(22, 22, 16, 22));
        content.add(new TicketView(student, assignment), BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);

        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }
}
