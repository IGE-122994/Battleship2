package battleship;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class PlayTimer {

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

}
