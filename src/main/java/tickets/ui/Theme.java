package tickets.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Оформление приложения: тема FlatLaf с палитрой «бумага и чернила», светлый и тёмный вариант.
 * Свои цвета хранятся в UIManager под ключами «Tickets.*» и читаются при каждой отрисовке,
 * поэтому смена темы подхватывается без перезапуска.
 */
public final class Theme {
    private static final String DARK_PREFERENCE = "darkTheme";

    private static final Map<String, String> COMMON = Map.ofEntries(
            Map.entry("Component.arc", "8"),
            Map.entry("Button.arc", "8"),
            Map.entry("TextComponent.arc", "8"),
            Map.entry("ScrollPane.smoothScrolling", "true"),
            Map.entry("Table.rowHeight", "32"),
            Map.entry("Table.showHorizontalLines", "true"),
            Map.entry("Table.gridColor", "$Tickets.line"),
            Map.entry("TableHeader.height", "32"),
            Map.entry("TableHeader.separatorColor", "$TableHeader.background"),
            Map.entry("TableHeader.bottomSeparatorColor", "$Tickets.line"),
            Map.entry("TitlePane.unifiedBackground", "true"),
            Map.entry("[style]Button.primary", "font: +1 $semibold.font; borderWidth: 0; default.borderWidth: 0;"
                    + " background: $Tickets.primary; foreground: $Tickets.onPrimary;"
                    + " hoverBackground: $Tickets.primaryHover; pressedBackground: $Tickets.primaryPressed;"
                    + " focusedBackground: $Tickets.primary; default.background: $Tickets.primary;"
                    + " default.foreground: $Tickets.onPrimary; default.hoverBackground: $Tickets.primaryHover;"
                    + " default.pressedBackground: $Tickets.primaryPressed; default.focusedBackground: $Tickets.primary;"
                    + " disabledBackground: $Tickets.disabledFill; disabledText: $Tickets.disabledText"),
            Map.entry("[style].muted", "foreground: $Tickets.muted"),
            Map.entry("[style].ink", "foreground: $Tickets.ink"),
            Map.entry("[style].errorText", "foreground: $Tickets.errorText"));

    private static final Map<String, String> LIGHT = Map.ofEntries(
            Map.entry("@background", "#F4F6F9"),
            Map.entry("@accentColor", "#1F4E79"),
            Map.entry("Tickets.ink", "#1F4E79"),
            Map.entry("Tickets.primary", "#1F4E79"),
            Map.entry("Tickets.primaryHover", "#285D8E"),
            Map.entry("Tickets.primaryPressed", "#173C5E"),
            Map.entry("Tickets.onPrimary", "#FFFFFF"),
            Map.entry("Tickets.disabledFill", "#DCE2E9"),
            Map.entry("Tickets.disabledText", "#7C8693"),
            Map.entry("Tickets.muted", "#5E6977"),
            Map.entry("Tickets.sidebar", "#EAEEF3"),
            Map.entry("Tickets.paper", "#FFFFFF"),
            Map.entry("Tickets.line", "#D5DBE3"),
            Map.entry("Tickets.repeatBackground", "#FBEAC6"),
            Map.entry("Tickets.repeatText", "#744600"),
            Map.entry("Tickets.errorBackground", "#FBECEC"),
            Map.entry("Tickets.errorBorder", "#E3A6A6"),
            Map.entry("Tickets.errorText", "#8E1C1C"),
            Map.entry("Tickets.stamp", "#B32432"));

    private static final Map<String, String> DARK = Map.ofEntries(
            Map.entry("@background", "#23272E"),
            Map.entry("@accentColor", "#5E8FD6"),
            Map.entry("Tickets.ink", "#A9C4EE"),
            Map.entry("Tickets.primary", "#3B6DB5"),
            Map.entry("Tickets.primaryHover", "#4579C2"),
            Map.entry("Tickets.primaryPressed", "#325E9E"),
            Map.entry("Tickets.onPrimary", "#FFFFFF"),
            Map.entry("Tickets.disabledFill", "#353C46"),
            Map.entry("Tickets.disabledText", "#8892A0"),
            Map.entry("Tickets.muted", "#9BA6B5"),
            Map.entry("Tickets.sidebar", "#1D2026"),
            Map.entry("Tickets.paper", "#2D333C"),
            Map.entry("Tickets.line", "#3E4652"),
            Map.entry("Tickets.repeatBackground", "#4E3F22"),
            Map.entry("Tickets.repeatText", "#F2CD88"),
            Map.entry("Tickets.errorBackground", "#472A2E"),
            Map.entry("Tickets.errorBorder", "#7E3D45"),
            Map.entry("Tickets.errorText", "#F5BCC1"),
            Map.entry("Tickets.stamp", "#EF7C86"));

    private Theme() {
    }

    /** Ставит тему, выбранную в прошлый раз (по умолчанию светлую). */
    public static void install() {
        apply(preferences().getBoolean(DARK_PREFERENCE, false));
    }

    static boolean isDark() {
        return FlatLaf.isLafDark();
    }

    /** Переключает светлую и тёмную тему во всех открытых окнах и запоминает выбор. */
    static void toggle() {
        boolean dark = !isDark();
        apply(dark);
        FlatLaf.updateUI();
        preferences().putBoolean(DARK_PREFERENCE, dark);
    }

    /** Цвет палитры по имени: ink, muted, sidebar, paper, line, repeatBackground, ... */
    static Color color(String name) {
        Color color = UIManager.getColor("Tickets." + name);
        return color != null ? color : Color.GRAY;
    }

    /** Шрифт с засечками — только для номера билета, остальной интерфейс на системном шрифте. */
    static Font serif(int style, float size) {
        return new Font(Serif.FAMILY, style, 1).deriveFont(size);
    }

    /** Graphics с включённым сглаживанием; вызывающий обязан сделать dispose(). */
    static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    private static void apply(boolean dark) {
        Map<String, String> defaults = new HashMap<>(COMMON);
        defaults.putAll(dark ? DARK : LIGHT);
        FlatLaf.setGlobalExtraDefaults(defaults);
        if (dark) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
    }

    private static Preferences preferences() {
        return Preferences.userNodeForPackage(Theme.class);
    }

    /** Ищется один раз, при первом обращении. */
    private static final class Serif {
        static final String FAMILY = pick();

        private static String pick() {
            List<String> installed = Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            for (String family : List.of("Georgia", "Cambria", "Times New Roman", "DejaVu Serif")) {
                if (installed.contains(family)) {
                    return family;
                }
            }
            return Font.SERIF;
        }
    }
}
