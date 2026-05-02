package battleship;

import org.joda.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class GameTimer.
 * Author: Diogo Almeida
 * Date: 2026-04-29 15:26
 * Cyclomatic Complexity:
 * - constructor: 1
 * - begin(): 1
 * - end(): 1
 * - getDuration(): 1
 * - formatDuration(Duration): 4
 */
public class GameTimerTest {

    private GameTimer timer;

    @BeforeEach
    public void setUp() {
        timer = new GameTimer();
    }

    @AfterEach
    public void tearDown() {
        timer = null;
    }

    /* -------------------------
     * constructor: CC = 1
     * ------------------------- */

    @DisplayName("Construtor do timer")
    @Test
    public void constructor() {
        // Verify that the constructor produces a non-null instance
        assertNotNull(timer, "Error: expected non-null GameTimer instance but got null");
    }

    /* -------------------------
     * begin(): CC = 1
     * ------------------------- */

    @DisplayName("Início do timer")
    @Test
    public void begin() {
        // Path: normal begin -> end -> getDuration should succeed
        timer.begin();
        timer.end();
        Duration d = timer.getDuration();
        assertNotNull(d, "Error: expected non-null Duration after begin/end but got null");
        assertTrue(d.getMillis() >= 0, "Error: expected non-negative duration but got " + d.getMillis());
    }

    /* -------------------------
     * end(): CC = 1
     * ------------------------- */

    @DisplayName("Término do timer")
    @Test
    public void end() {
        // Path: normal begin -> end -> getDuration should succeed (same as begin path but focused on end)
        timer.begin();
        timer.end();
        Duration d = timer.getDuration();
        assertAll(
                () -> assertNotNull(d, "Error: expected non-null Duration after end but got null"),
                () -> assertTrue(d.getMillis() >= 0, "Error: expected non-negative duration after end but got " + d.getMillis())
        );
    }

    /* -------------------------
     * getDuration(): CC = 1
     * ------------------------- */

    @DisplayName("Duração do timer")
    @Test
    public void getDuration() {
        // Path 1: normal usage (begin then end)
        timer.begin();
        timer.end();
        Duration normal = timer.getDuration();
        assertNotNull(normal, "Error: expected non-null Duration for normal begin/end but got null");
        assertTrue(normal.getMillis() >= 0, "Error: expected non-negative duration for normal begin/end but got " + normal.getMillis());
    }

    /* -------------------------
     * formatDuration(Duration): CC = 4
     * ------------------------- */

    @DisplayName("Formatação do timer - Apenas millis")
    @Test
    public void formatDuration1() {
        // Path: hours == 0, minutes == 0, seconds == 0 -> output should be "Xms"
        Duration d = new Duration(123); // 123 ms
        String formatted = GameTimer.formatDuration(d);
        assertEquals("123ms", formatted, "Error: expected '123ms' but got '" + formatted + "'");
    }

    @DisplayName("Formatação do timer - Segundos e millis")
    @Test
    public void formatDuration2() {
        // Path: seconds > 0, minutes == 0, hours == 0 -> output should include seconds and millis
        Duration d = new Duration(4_812); // 4 seconds and 812 ms
        String formatted = GameTimer.formatDuration(d);
        assertEquals("4s 812ms", formatted, "Error: expected '4s 812ms' but got '" + formatted + "'");
    }

    @DisplayName("Formatação do timer - Minutos, segundos e millis")
    @Test
    public void formatDuration3() {
        // Path: minutes > 0, hours == 0 -> output should include minutes, seconds and millis
        Duration d = new Duration((2 * 60 + 36) * 1000L + 469); // 2m 36s 469ms
        String formatted = GameTimer.formatDuration(d);
        assertEquals("2m 36s 469ms", formatted, "Error: expected '2m 36s 469ms' but got '" + formatted + "'");
    }

    @DisplayName("Formatação do timer - Horas, minutos, segundos e millis")
    @Test
    public void formatDuration4() {
        // Path: hours > 0 -> output should include hours, minutes, seconds and millis
        Duration d = new Duration(((1L * 3600) + (12 * 60) + 5) * 1000L + 327); // 1h 12m 5s 327ms
        String formatted = GameTimer.formatDuration(d);
        assertEquals("1h 12m 5s 327ms", formatted, "Error: expected '1h 12m 5s 327ms' but got '" + formatted + "'");
    }

    @DisplayName("Formatação do timer - Exatamente uma hora")
    @Test
    public void formatDuration5() {
        // Path: exact hour -> hours>0 true, minutes==0, seconds==0
        // This exercises the minutes condition being true because hours>0 while minutes==0 (atomic-condition case).
        Duration d = new Duration(1L * 3_600_000); // exactly 1 hour -> 1h 0m 0s 0ms
        String formatted = GameTimer.formatDuration(d);
        assertEquals("1h 0m 0s 0ms", formatted, "Error: expected '1h 0m 0s 0ms' but got '" + formatted + "'");
    }

    @DisplayName("Formatação do timer - Exatamente dois minutos")
    @Test
    public void formatDuration6() {
        // Path: exact minute -> minutes>0 true, seconds==0, hours==0
        // This exercises the seconds condition being true because minutes>0 while seconds==0 (atomic-condition case).
        Duration d = new Duration(2L * 60 * 1000); // exactly 2 minutes -> 2m 0s 0ms
        String formatted = GameTimer.formatDuration(d);
        assertEquals("2m 0s 0ms", formatted, "Error: expected '2m 0s 0ms' but got '" + formatted + "'");
    }
}