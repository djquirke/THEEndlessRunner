package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * Created by Dan on 24/02/2016.
 */
public class Animation {
    private Bitmap[] frames;
    private int currentFrame;
    private Stopwatch timeSinceFrameChange = new Stopwatch();
    private long delay;
    private boolean playedOnce;
    private Rect collisionRect = new Rect();

    public void setFrames(Bitmap[] frames)
    {
        this.frames = frames;
        currentFrame = 0;
        timeSinceFrameChange.start();
    }

    public void setDelay(long d) {delay = d;}
    public void setFrame(int i) {currentFrame = i;}

    public void update()
    {
        if(timeSinceFrameChange.elapsed() > delay)
        {
            currentFrame++;
            timeSinceFrameChange.start();
        }

        if(currentFrame == frames.length)
        {
            currentFrame = 0;
            playedOnce = true;
        }
    }

    public Bitmap getImage() {return frames[currentFrame];}
    public int getFrame() {return currentFrame;}
    public boolean playedOnce() {return playedOnce;}
    public Rect getColRect() {return collisionRect;}
    public void setColRect(Rect rect) {collisionRect = rect;}
    public void setColRect(int left, int top, int right, int bottom)
    {
        collisionRect.left = left;
        collisionRect.top = top;
        collisionRect.right = right;
        collisionRect.bottom = bottom;
    }

}
