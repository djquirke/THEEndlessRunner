package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Bitmap;
import android.graphics.Rect;


public class Animation {
    private Bitmap[] frames;
    private int currentFrame;
    private Stopwatch timeSinceFrameChange = new Stopwatch();
    private long delay;
    private Rect collisionRect = new Rect();

    public void setFrames(Bitmap[] frames)
    {
        this.frames = frames;
        currentFrame = 0;
        timeSinceFrameChange.start();
    }


    public void update()
    {
        if(timeSinceFrameChange.elapsed() > delay)
        {
            currentFrame++;
            timeSinceFrameChange.start();
        }

        if(currentFrame == frames.length) currentFrame = 0;
    }

    public Bitmap getImage() {return frames[currentFrame];}
    public Rect getColRect() {return collisionRect;}
    public void setColRect(Rect rect) {collisionRect = rect;}
    public void setDelay(long d) {delay = d;}
    public void setFrame(int i) {currentFrame = i;}
}
