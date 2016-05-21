package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class MainThread extends Thread
{
    private final static int FPS = 60;
    private final static int FRAME_PERIOD = 1000 / FPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel)
    {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run()
    {
        Canvas canvas;

        long beginTime;
        long timeDif;
        int sleepTime;

        while(running)
        {
            canvas = null;

            try
            {
                canvas = this.surfaceHolder.lockCanvas();

                synchronized (surfaceHolder)
                {
                    beginTime = System.currentTimeMillis();
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);

                    timeDif = System.currentTimeMillis() - beginTime;

                    sleepTime = (int)(FRAME_PERIOD - timeDif);

                    if(sleepTime > 0)
                    {
                        try {Thread.sleep(sleepTime);}
                        catch (InterruptedException e) {}
                    }
                }
            }
            finally
            {
                if(canvas != null)
                {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void setRunning(boolean running)
    {
        this.running = running;
    }
}
