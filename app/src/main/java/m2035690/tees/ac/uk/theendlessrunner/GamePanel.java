package m2035690.tees.ac.uk.theendlessrunner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Vector;

/**
 * Created by Dan on 07/02/2016.
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    private MainThread thread;
    public static float HEIGHT;
    public static float WIDTH;
    public static float DENSITY;
    private static Player player;
    public static Vector2f player_offset;
    public static Camera camera;
    private static Vector<Spike> spikes = new Vector<Spike>();
    private Vector2f downCoords, upCoords;
    private static float SWIPE_DISTANCE_THRESHOLD;
    private static final float SWIPE_TIME_THRESHOLD = 0.5f;

    private static final long SPIKE_SPAWN_SPEED = 1000;
    private long startTime;

    public GamePanel(Context context)
    {
        super(context);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        DENSITY = displayMetrics.density;
        WIDTH = displayMetrics.widthPixels / DENSITY;
        HEIGHT = displayMetrics.heightPixels / DENSITY;
        System.out.println(WIDTH + " " + HEIGHT + " " + DENSITY);

        SWIPE_DISTANCE_THRESHOLD = HEIGHT / 5;

        camera = new Camera(0, 0, (int)WIDTH, (int)HEIGHT);
        player_offset = new Vector2f(100, HEIGHT / 2 - Utils.pixToDip(64)); //replace with point loaded from map

        downCoords = new Vector2f();
        upCoords = new Vector2f();

        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        boolean retry = true;
        int counter = 0;

        while(retry && counter < 100)
        {
            counter++;
            try
            {
                thread.setRunning(false);
                thread.join();
                retry = false;
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        player = new Player(BitmapFactory.decodeResource(getResources(), R.mipmap.characters),
                            player_offset,
                            230, 230);
        player.addAnimation("run", 0, 0, 230, 230, 8, 80, true);
        player.addAnimation("jump", 0, 230, 230, 230, 2, 10000, false);
        player.setColRect((int) Utils.pixToDip(55), 0, (int) Utils.pixToDip(55), 0);

        //safely start game loop
        thread.setRunning(true);
        thread.start();

        startTime = System.nanoTime();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if(!player.getPlaying())
                {
                    player.setPlaying(true);
                    break;
                }
                downCoords.x = Utils.pixToDip(event.getX());
                downCoords.y = Utils.pixToDip(event.getY());
                return true;
            case MotionEvent.ACTION_UP:
                upCoords.x = Utils.pixToDip(event.getX());
                upCoords.y = Utils.pixToDip(event.getY());

                float xdif = upCoords.x - downCoords.x;
                float ydif = upCoords.y - downCoords.y;
                //System.out.println(xdif + " " + ydif + " " + Math.abs(xdif) + " " + Math.abs(ydif));
                if(Math.abs(ydif) > Math.abs(xdif) && Math.abs(ydif) > SWIPE_DISTANCE_THRESHOLD)
                {
                    if(ydif < 0)
                    {
                        player.Jump();
                    }
                    else
                    {
                        //slide
                    }
                }

                return true;
        }

        return super.onTouchEvent(event);
    }

    public void update()
    {
        if(player.getPlaying())
        {
            player.update();
            camera.setCamera(player.getX() - player_offset.x, player.getY() - player_offset.y,
                             (int)WIDTH, (int)HEIGHT);

            long elapsed = (System.nanoTime() - startTime) / 1000000;
            if(elapsed > SPIKE_SPAWN_SPEED)
            {
                Bitmap spike = BitmapFactory.decodeResource(getResources(), R.mipmap.spike);
                Spike temp = new Spike(spike,
                        new Vector2f(player.getX() + 1000, player_offset.y + player.getHeight() - Utils.pixToDip(spike.getHeight())),
                        5);
                temp.setColRect((int)Utils.pixToDip(40), 0, (int)Utils.pixToDip(40), 0);
                spikes.add(temp);

                startTime = System.nanoTime();
            }
        }
    }

    @Override
    public void draw(Canvas canvas)
    {
        if(canvas != null)
        {
            canvas.drawColor(Color.BLACK); //reset canvas to black

            player.draw(canvas);
            for(Spike spike : spikes)
            {
                spike.draw(canvas);
            }
        }
    }

    public static boolean checkCollision()
    {
        boolean ret = false;

        for(Spike spike : spikes)
        {
            if(spike.getColRect().intersect(player.getColRect()))
            {
                System.out.println("Collision detected, player rect: " + player.getColRect().left + " " +
                                   player.getColRect().top + " " + player.getColRect().right + " " +
                                   player.getColRect().bottom + ", spike: " + spike.getColRect().left + " " +
                        spike.getColRect().top + " " + spike.getColRect().right + " " +
                        spike.getColRect().bottom);
                ret = true;
            }
        }

        return ret;
    }
}
