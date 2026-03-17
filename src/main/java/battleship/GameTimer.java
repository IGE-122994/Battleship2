package battleship;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Utility class responsible for measuring real-time durations during the game.
 * <p>
 * The {@code GameTimer} records the start and end instants of a time interval
 * using the Joda-Time library. It is used to measure:
 * <ul>
 *     <li>The total duration of the game</li>
 *     <li>The duration of each individual player turn</li>
 * </ul>
 * The timer must be explicitly started with {@link #begin()} and stopped with
 * {@link #end()}. The elapsed time can then be retrieved via {@link #getDuration()}.
 * </p>
 *
 * <p>
 * The class also provides a human-readable formatter through
 * {@link #formatDuration(Duration)}, producing compact output such as:
 * {@code "2h 15m 25s 327ms"}.
 * </p>
 */
public class GameTimer {

    private DateTime beginning;
    private DateTime ending;

    /**
     * Marks the beginning of the timed interval.
     * <p>
     * This method should be called at the moment the game starts or at the
     * beginning of the first turn, depending on the context in which the timer
     * is used.
     * </p>
     */
    public void begin() {
        beginning = DateTime.now();
    }

    /**
     * Marks the end of the timed interval.
     * <p>
     * After calling this method, the duration between {@link #begin()} and
     * {@link #end()} can be retrieved using {@link #getDuration()}.
     * </p>
     */
    public void end() {
        ending = DateTime.now();
    }

    /**
     * Returns the elapsed duration between the last calls to {@link #begin()}
     * and {@link #end()}.
     *
     * @return a {@link Duration} representing the elapsed time
     */
    public Duration getDuration() {
        return new Duration(beginning, ending);
    }

    /**
     * Formats a {@link Duration} into a compact, human-readable string.
     * <p>
     * The format adapts to the magnitude of the duration and may include hours,
     * minutes, seconds and milliseconds. Examples:
     * <ul>
     *     <li>{@code "4s 812ms"}</li>
     *     <li>{@code "2m 36s 469ms"}</li>
     *     <li>{@code "1h 12m 5s 327ms"}</li>
     * </ul>
     *
     * @param d the duration to format
     * @return a formatted string representing the duration
     */
    public static String formatDuration(Duration d) {
        long totalMs = d.getMillis();

        long hours = totalMs / 3_600_000;
        long minutes = (totalMs % 3_600_000) / 60_000;
        long seconds = (totalMs % 60_000) / 1000;
        long millis = totalMs % 1000;

        StringBuilder sb = new StringBuilder();

        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || minutes > 0 || hours > 0) sb.append(seconds).append("s ");
        sb.append(millis).append("ms");

        return sb.toString().trim();
    }

}