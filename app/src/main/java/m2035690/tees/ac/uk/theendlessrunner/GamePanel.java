package m2035690.tees.ac.uk.theendlessrunner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
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
    private static Vector<Spike> spikes = new Vector<>();
    private static Vector<Wall> walls = new Vector<>();
    private Vector2f downCoords, upCoords;
    private static float SWIPE_DISTANCE_THRESHOLD;
    private static final int SWIPE_TIME_THRESHOLD = 500;

    private static final long SPIKE_SPAWN_SPEED = 1000;
    private Stopwatch spikeLastSpawnTime = new Stopwatch();
    private Stopwatch swipeTime = new Stopwatch();

    public GamePanel(Context context)
    {
        super(context);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        DENSITY = displayMetrics.density;
        WIDTH = displayMetrics.widthPixels / DENSITY;
        HEIGHT = displayMetrics.heightPixels / DENSITY;
        //System.out.println(WIDTH + " " + HEIGHT + " " + DENSITY);

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
                            230, 230, 100);
        player.addAnimation("run", 0, 0, 230, 230, 8, 80,
                            new Rect(Utils.pixToDip(55), 0, Utils.pixToDip(55), 0), true);
        player.addAnimation("jump", 0, 230, 230, 230, 2, 10000,
                            new Rect(Utils.pixToDip(55), 0, Utils.pixToDip(55), 0), false);
        player.addAnimation("slide", 0, 460, 230, 230, 1, 10000,
                            new Rect(Utils.pixToDip(55), Utils.pixToDip(70), 0, 0), false);

        spikes.clear();
        walls.clear();

        //TODO: remove when proper level loading complete
        Bitmap wall_img = BitmapFactory.decodeResource(getResources(), R.mipmap.wall);
        //temp: add 100 floor tiles
        for(int i = 0; i < 100; i++)
        {
            Wall temp = new Wall(wall_img,
                                 new Vector2f(player_offset.x + i * Utils.pixToDip(wall_img.getWidth()),
                                              player_offset.y + player.getHeight()));

            walls.add(temp);

        }
        Wall temp = new Wall(wall_img,
                new Vector2f(player_offset.x + 300,
                        player_offset.y - 1.25f * player.getHeight()));
        walls.add(temp);
        Wall temp2 = new Wall(wall_img,
                new Vector2f(player_offset.x + 900,
                        player_offset.y - 3 * player.getHeight()));
        walls.add(temp2);

        //safely start game loop
        thread.setRunning(true);
        thread.start();

        spikeLastSpawnTime.start();
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
                swipeTime.start();
                return true;
            case MotionEvent.ACTION_UP:
                upCoords.x = Utils.pixToDip(event.getX());
                upCoords.y = Utils.pixToDip(event.getY());

                float xdif = upCoords.x - downCoords.x;
                float ydif = upCoords.y - downCoords.y;

                if(Math.abs(ydif) > Math.abs(xdif) &&
                         Math.abs(ydif) > SWIPE_DISTANCE_THRESHOLD &&
                         swipeTime.elapsed() < SWIPE_TIME_THRESHOLD)
                {
                    if(ydif < 0) player.Jump();
                    else player.Slide();
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
            
            if(spikeLastSpawnTime.elapsed() > SPIKE_SPAWN_SPEED)
            {
                Bitmap spike = BitmapFactory.decodeResource(getResources(), R.mipmap.spike);
                Spike temp = new Spike(spike,
                        new Vector2f(player.getX() + 1000, player_offset.y + player.getHeight() - Utils.pixToDip(spike.getHeight())),
                        5);
                temp.setColRect(Utils.pixToDip(40), 0, Utils.pixToDip(40), 0);
                spikes.add(temp);

                spikeLastSpawnTime.start();
            }

            //check collisions
            if(player.getAlive())
                checkCollisions();
        }
    }

    @Override
    public void draw(Canvas canvas)
    {
        if(canvas != null)
        {
            canvas.drawColor(Color.YELLOW); //reset canvas to black

            for(Spike spike : spikes)
            {
                //spike.drawDebug(canvas, Color.RED);
                spike.draw(canvas);
            }
            for(Wall wall : walls)
            {
                wall.draw(canvas);
                //wall.drawDebug(canvas, Color.GREEN);
            }
            player.draw(canvas);
        }
    }

    public void checkCollisions()
    {
        for(Spike spike : spikes)
        {
            player.checkCollision(spike);
        }

        for(Wall wall : walls)
        {
            player.checkCollision(wall);
        }

        //player.collisionCheckComplete();
    }

    public static void Reset()
    {
        spikes.clear();
    }
}
