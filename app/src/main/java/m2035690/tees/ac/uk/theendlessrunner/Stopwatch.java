package m2035690.tees.ac.uk.theendlessrunner;

public class Stopwatch {
    private long startTime = 0;
    private long pauseTime = 0;
    public boolean is_running = false;

    public Stopwatch() {}

    public void start()
    {
        startTime = System.currentTimeMillis();
        is_running = true;
    }

    public long elapsed()
    {
        long time = System.currentTimeMillis();
        return time - startTime;
    }

    public void pause()
    {
        pauseTime = System.currentTimeMillis();
        is_running = false;
    }

    public void resume()
    {
        long time = System.currentTimeMillis();
        long pauseLength = time - pauseTime;
        startTime += pauseLength;
        is_running = true;
    }

    public void stop()
    {
        is_running = false;
    }
}
