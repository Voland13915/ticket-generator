package tickets.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlTest {

    @Test
    void specialCharactersAreEscaped() {
        assertEquals("a &lt; b &amp;&amp; c &gt; d", Html.escape("a < b && c > d"));
    }

    @Test
    void questionTextCannotInjectMarkup() {
        assertEquals("&lt;b&gt;жирный&lt;/b&gt;", Html.escape("<b>жирный</b>"));
    }
}
