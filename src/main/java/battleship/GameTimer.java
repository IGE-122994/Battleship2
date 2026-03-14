package battleship;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class GameTimer {

    private DateTime beginning;
    private DateTime ending;

    public void begin() {
        beginning = DateTime.now();
    }

    public void end() {
        ending = DateTime.now();
    }

    public Duration getDuration() {
        return new Duration(beginning, ending);
    }

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