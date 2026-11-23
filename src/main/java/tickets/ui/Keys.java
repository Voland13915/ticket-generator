package tickets.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;

/** Горячие клавиши уровня окна. */
final class Keys {

    private Keys() {
    }

    static void bind(JRootPane root, int keyCode, Runnable action) {
        String name = "key-" + keyCode;
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0), name);
        root.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }
}
