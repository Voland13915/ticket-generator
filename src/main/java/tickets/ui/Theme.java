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
            Map.entry("Table.rowHeight", "36"),
            Map.entry("Table.showHorizontalLines", "false"),
            Map.entry("Table.showVerticalLines", "false"),
            Map.entry("Table.intercellSpacing", "0,0"),
            Map.entry("Table.background", "$Tickets.paper"),
            Map.entry("Table.selectionBackground", "$Tickets.selection"),
            Map.entry("Table.selectionForeground", "$Table.foreground"),
            Map.entry("Table.selectionInactiveBackground", "$Tickets.selection"),
            Map.entry("Table.selectionInactiveForeground", "$Table.foreground"),
            Map.entry("Table.cellFocusColor", "$Tickets.selection"),
            Map.entry("TableHeader.height", "36"),
            Map.entry("TableHeader.background", "$Tickets.paper"),
            Map.entry("TableHeader.foreground", "$Tickets.muted"),
            Map.entry("TableHeader.separatorColor", "$Tickets.paper"),
            Map.entry("TableHeader.bottomSeparatorColor", "$Tickets.line"),
            Map.entry("ComboBox.padding", "4,8,4,8"),
            Map.entry("ToolTip.border", "6,10,6,10"),
            Map.entry("TitlePane.unifiedBackground", "true"),
            Map.entry("[style]Button.primary", "font: +1 $semibold.font; borderWidth: 0; default.borderWidth: 0;"
                    + " background: $Tickets.primary; foreground: $Tickets.onPrimary;"
                    + " hoverBackground: $Tickets.primaryHover; pressedBackground: $Tickets.primaryPressed;"
                    + " focusedBackground: $Tickets.primary; default.background: $Tickets.primary;"
                    + " default.foreground: $Tickets.onPrimary; default.hoverBackground: $Tickets.primaryHover;"
                    + " default.pressedBackground: $Tickets.primaryPressed; default.focusedBackground: $Tickets.primary;"
                    + " disabledBackground: $Tickets.disabledFill; disabledText: $Tickets.disabledText"),
            Map.entry("[style]Button.chip", "arc: 999; borderWidth: 0; focusWidth: 0; innerFocusWidth: 0;"
                    + " margin: 4,12,4,12; iconTextGap: 6; font: $semibold.font;"
                    + " background: $Tickets.repeatBackground; foreground: $Tickets.repeatText;"
                    + " hoverBackground: $Tickets.warnHover; pressedBackground: $Tickets.warnHover"),
            Map.entry("[style]Button.quiet", "arc: 10; borderWidth: 0; focusWidth: 0; innerFocusWidth: 0;"
                    + " margin: 5,10,5,10; iconTextGap: 7; background: $Tickets.background;"
                    + " hoverBackground: $Tickets.hover; pressedBackground: $Tickets.line"),
            Map.entry("[style].muted", "foreground: $Tickets.muted"),
            Map.entry("[style].onPrimary", "foreground: $Tickets.onPrimary"),
            Map.entry("[style].onPrimaryMuted", "foreground: $Tickets.onPrimaryMuted"),
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
            Map.entry("Tickets.stamp", "#B32432"),
            Map.entry("Tickets.background", "#F4F6F9"),
            Map.entry("Tickets.hover", "#E3E8EF"),
            Map.entry("Tickets.selection", "#DCE8F5"),
            Map.entry("Tickets.stripe", "#F7F9FB"),
            Map.entry("Tickets.badge", "#E6EEF7"),
            Map.entry("Tickets.warnHover", "#F6DDA8"),
            Map.entry("Tickets.success", "#2E8B57"),
            Map.entry("Tickets.shadow", "#1F3A5A"),
            Map.entry("Tickets.onPrimaryMuted", "#B9CDE3"));

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
            Map.entry("Tickets.stamp", "#EF7C86"),
            Map.entry("Tickets.background", "#23272E"),
            Map.entry("Tickets.hover", "#2F353E"),
            Map.entry("Tickets.selection", "#33465F"),
            Map.entry("Tickets.stripe", "#30363F"),
            Map.entry("Tickets.badge", "#36465C"),
            Map.entry("Tickets.warnHover", "#5E4B27"),
            Map.entry("Tickets.success", "#6CC08F"),
            Map.entry("Tickets.shadow", "#000000"),
            Map.entry("Tickets.onPrimaryMuted", "#C7D7EF"));

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
