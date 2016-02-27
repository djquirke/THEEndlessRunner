package m2035690.tees.ac.uk.theendlessrunner;

/**
 * Created by Dan on 27/02/2016.
 */
public class Stopwatch {
    private long startTime = 0;

    public Stopwatch() {}

    public void start()
    {
        startTime = System.currentTimeMillis();
    }

    public long elapsed()
    {
        long time = System.currentTimeMillis();
        return time - startTime;
    }
}
