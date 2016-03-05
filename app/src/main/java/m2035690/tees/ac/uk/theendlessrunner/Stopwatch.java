package m2035690.tees.ac.uk.theendlessrunner;

public class Stopwatch {
    private long startTime = 0;
    private long pauseTime = 0;

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

    public void pause()
    {
        pauseTime = System.currentTimeMillis();
    }

    public void resume()
    {
        long time = System.currentTimeMillis();
        long pauseLength = time - pauseTime;
        startTime += pauseLength;
    }
}
