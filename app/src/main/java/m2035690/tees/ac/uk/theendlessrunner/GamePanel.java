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
    public static Vector2f player_spawn;
    public static Vector2f camera_offset;
    public static Camera camera;
    private static Vector<GameObject> entities = new Vector<GameObject>();
    private Vector2f downCoords, upCoords;
    private static float SWIPE_DISTANCE_THRESHOLD;
    private static final int SWIPE_TIME_THRESHOLD = 500;
    private static int TILE_SIZE;
    private Stopwatch swipeTime = new Stopwatch();
    private Map map = new Map();

    public GamePanel(Context context)
    {
        super(context);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        DENSITY = displayMetrics.density;
        WIDTH = displayMetrics.widthPixels / DENSITY;
        HEIGHT = displayMetrics.heightPixels / DENSITY;
        //System.out.println(WIDTH + " " + HEIGHT + " " + DENSITY);

        SWIPE_DISTANCE_THRESHOLD = HEIGHT / 5;

        TILE_SIZE = Utils.pixToDip(333);

        camera = new Camera(0, 0, (int)WIDTH, (int)HEIGHT);
        camera_offset = new Vector2f(WIDTH / 8, HEIGHT / 2);
        System.out.println(TILE_SIZE);

        downCoords = new Vector2f();
        upCoords = new Vector2f();

        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    private void loadMap(String name)
    {
        Bitmap temp = BitmapFactory.decodeResource(getResources(), R.mipmap.map01);
        map.setHeight(temp.getHeight() * TILE_SIZE);
        map.setWidth(temp.getWidth() * TILE_SIZE);
        camera.setMapHeight(map.getHeight());
        camera.setMapWidth(map.getWidth());

        Bitmap wall_img = BitmapFactory.decodeResource(getResources(), R.mipmap.wall);
        Bitmap wall_slide_img = BitmapFactory.decodeResource(getResources(), R.mipmap.wall_slide);
        Bitmap spike_img = BitmapFactory.decodeResource(getResources(), R.mipmap.spike);
        Bitmap player_img = BitmapFactory.decodeResource(getResources(), R.mipmap.characters);

        for(int i = 0; i < temp.getHeight(); i++)
        {
            for(int j = 0; j < temp.getWidth(); j++)
            {
                int p = temp.getPixel(j, i);
                if(p == -1) continue;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                if(r == 0 & g == 0 && b == 0) //BLACK = WALL
                {
//                    System.out.println("wall found, drawing at:" + j * TILE_SIZE + " " + i * TILE_SIZE);
                    Wall tempw = new Wall(wall_img, new Vector2f(j * TILE_SIZE, i * TILE_SIZE));
                    entities.add(tempw);
                }
                else if(r == 255 && g == 0 && b == 0) //RED = SPIKES
                {
//                    System.out.println("spike found, drawing at:" + j * TILE_SIZE + " " + i * TILE_SIZE);
                    Spike tempsp = new Spike(spike_img, new Vector2f(j * TILE_SIZE,
                                             i * TILE_SIZE + (TILE_SIZE - Utils.pixToDip(spike_img.getHeight()))));

                    tempsp.setColRect(Utils.pixToDip(40), 0, Utils.pixToDip(40), 0);
                    entities.add(tempsp);
                }
                else if(r == 0 && g == 255 && b == 0) //GREEN = PLAYER SPAWN
                {
//                    System.out.println("player spawn found, drawing at:" + j * TILE_SIZE + " " + i * TILE_SIZE);
                    player = new Player(player_img, new Vector2f(j * TILE_SIZE, i * TILE_SIZE), 230, 230, 100);
                    player.addAnimation("run", 0, 0, 230, 230, 8, 80,
                            new Rect(Utils.pixToDip(55), 0, Utils.pixToDip(55), 0), true);
                    player.addAnimation("jump", 0, 230, 230, 230, 2, 10000,
                            new Rect(Utils.pixToDip(55), 0, Utils.pixToDip(55), 0), false);
                    player.addAnimation("slide", 0, 460, 230, 230, 1, 10000,
                            new Rect(Utils.pixToDip(55), Utils.pixToDip(70), 0, 0), false);

                    player_spawn = new Vector2f(j * TILE_SIZE, i * TILE_SIZE);

                    camera_offset.y -= Utils.pixToDip(230) / 2;
                    camera.setCamera(player_spawn.x - camera_offset.x, player_spawn.y - camera_offset.y);
                }
                else if(r == 127 && g == 127 && b == 127) //GREY = SLIDE UNDER WALL
                {
//                    System.out.println("wall slide found, drawing at:" + j * TILE_SIZE + " " + i * TILE_SIZE);
                    Wall tempw = new Wall(wall_slide_img, new Vector2f(j * TILE_SIZE, i * TILE_SIZE));
                    entities.add(tempw);
                }
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        entities.clear();

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
        loadMap("map01");

        //safely start game loop
        thread.setRunning(true);
        thread.start();
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

            //check collisions
            if(player.getAlive())
            {
                camera.setCamera(player.getX() - camera_offset.x, player.getY() - camera_offset.y);
                checkCollisions();
            }
        }
    }

    @Override
    public void draw(Canvas canvas)
    {
        if(canvas != null)
        {
            canvas.drawColor(Color.YELLOW); //reset canvas to black

            for(GameObject obj : entities)
            {
                obj.draw(canvas);
                obj.drawDebug(canvas, Color.BLACK);
            }

            player.drawDebug(canvas, Color.MAGENTA);
            player.draw(canvas);
        }
    }

    public void checkCollisions()
    {
        for(GameObject obj : entities)
        {
            player.checkCollision(obj);
        }

//        for(Wall wall : walls)
//        {
//            player.checkCollision(wall);
//        }

        player.collisionCheckComplete();
    }

    public static void Reset()
    {
        //entities.clear();
        player.setPos(player_spawn);
        //TODO: reload level
    }
}
