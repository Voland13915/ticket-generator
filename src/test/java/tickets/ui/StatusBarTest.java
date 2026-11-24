package tickets.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusBarTest {

    @Test
    void showsMessageWithItsTone() {
        StatusBar bar = new StatusBar("results.xlsx");

        bar.show("Билет № 7 выдан", StatusBar.Tone.OK);
        assertEquals("Билет № 7 выдан", bar.text());
        assertEquals(StatusBar.Tone.OK, bar.tone());

        bar.show("Сохранение отменено", StatusBar.Tone.WARNING);
        assertEquals(StatusBar.Tone.WARNING, bar.tone());
    }
}
