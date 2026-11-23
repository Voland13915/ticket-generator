package tickets.ui;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;

/** Надпись, которая переносится по фактической ширине (в отличие от HTML в JLabel). Только для чтения. */
final class WrappingText extends JTextArea {

    WrappingText(Object styleClass) {
        setEditable(false);
        setFocusable(false);
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        putClientProperty(FlatClientProperties.STYLE_CLASS, styleClass);
    }
}
