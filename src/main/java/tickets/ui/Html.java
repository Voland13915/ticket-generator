package tickets.ui;

/** Экранирование текста для HTML-подписей Swing. */
final class Html {

    private Html() {
    }

    static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
