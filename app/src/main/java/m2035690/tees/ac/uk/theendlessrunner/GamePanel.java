package m2035690.tees.ac.uk.theendlessrunner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Vector;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener
{
    private MainThread thread;
    public static float HEIGHT;
    public static float WIDTH;
    public static float DENSITY;
    private static int TILE_SIZE;

    public static Vector2f player_spawn;
    public static Vector2f camera_offset;
    private Vector2f camera_translation_offset = new Vector2f();
    private static Player player;
    public static Camera camera;
    private static int num_coins = 0;
    private Vector2f mapDims = new Vector2f();

    private static Vector<GameObject> entities = new Vector<>();

    //sliding/jumping
    private Vector2f downCoords = new Vector2f(), upCoords = new Vector2f();
    private static float SWIPE_DISTANCE_THRESHOLD;
    private static final int SWIPE_TIME_THRESHOLD = 500;
    private Stopwatch swipeTime = new Stopwatch();

    //gyro scanning
    private float gyroX, gyroY;
    private Stopwatch scanningTime = new Stopwatch();
    private static final int SCAN_ENV_TIME = 5000;

    //double tap
    private Stopwatch doubleTapTime = new Stopwatch();
    private static final int DOUBLE_TAP_TIME_THRESHOLD = 250;
    private boolean tappedOnce = false;

    private Context m_context;

    private Stopwatch camera_transition_step = new Stopwatch();
    private Vector2f target_camera_pos = new Vector2f();
    private Vector2f start_camera_pos = new Vector2f();
    private Stopwatch cooldown_timer = new Stopwatch();
    private int time_to_cooldown;
    private Bitmap gyro_scan_img;

    private State game_state = State.NONE;
    private float prev_accelX, prev_accelY, prev_accelZ;
    private float new_accelX, new_accelY, new_accelZ;
    private float angle = 0;
    private float prev_angle = 0;
    private float goal_angle = 0;
    public static Orientation orientation = Orientation.LANDSCAPE;
    private Stopwatch accelerometer_rotate_time = new Stopwatch();
    private Orientation orientation_last_frame = Orientation.LANDSCAPE;
    private Stopwatch angle_lerp_timer = new Stopwatch();

    public GamePanel(Context context)
    {
        super(context);

        m_context = context;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        DENSITY = displayMetrics.density;
        WIDTH = displayMetrics.widthPixels / DENSITY;
        HEIGHT = displayMetrics.heightPixels / DENSITY;

        SWIPE_DISTANCE_THRESHOLD = HEIGHT / 5;

        Bitmap temp = BitmapFactory.decodeResource(getResources(), R.mipmap.wall);
        TILE_SIZE = Utils.pixToDip(temp.getWidth());
        temp.recycle();

        camera = new Camera(0, 0, (int)WIDTH, (int)HEIGHT);
        camera_offset = new Vector2f(WIDTH / 8, HEIGHT / 2);

        camera.InitialiseScanRect();
        gyro_scan_img = BitmapFactory.decodeResource(getResources(), R.mipmap.gyroradius);
        gyro_scan_img = Bitmap.createScaledBitmap(gyro_scan_img,
                Utils.dipToPix(camera.getScanRect().width()),
                Utils.dipToPix(camera.getScanRect().height()), false);

        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        loadMap();

        thread = new MainThread(getHolder(), this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);

    }

    private void loadMap()
    {
        Bitmap temp = BitmapFactory.decodeResource(getResources(), R.mipmap.map01);
        mapDims.y = temp.getHeight() * TILE_SIZE;
        mapDims.x = temp.getWidth() * TILE_SIZE;
        camera.setMapSize(mapDims);

        Bitmap wall_img = BitmapFactory.decodeResource(getResources(), R.mipmap.wall);
        Bitmap wall_slide_img = BitmapFactory.decodeResource(getResources(), R.mipmap.wall_slide);
        Bitmap spike_img = BitmapFactory.decodeResource(getResources(), R.mipmap.spike);
        Bitmap player_img = BitmapFactory.decodeResource(getResources(), R.mipmap.characters);
        Bitmap door_img = BitmapFactory.decodeResource(getResources(), R.mipmap.doorexit);
        Bitmap coin_img = BitmapFactory.decodeResource(getResources(), R.mipmap.coin);

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
                    Wall tempw = new Wall(wall_img, new Vector2f(j * TILE_SIZE, i * TILE_SIZE));
                    entities.add(tempw);
                }
                else if(r == 255 && g == 0 && b == 0) //RED = SPIKES
                {
                    Spike tempsp = new Spike(spike_img, new Vector2f(j * TILE_SIZE,
                                             i * TILE_SIZE + (TILE_SIZE - Utils.pixToDip(spike_img.getHeight()))));

                    tempsp.setColRect(Utils.pixToDip(40), 0, Utils.pixToDip(40), 0);
                    entities.add(tempsp);
                }
                else if(r == 0 && g == 255 && b == 0) //GREEN = PLAYER SPAWN
                {
                    int size = Utils.dipToPix(TILE_SIZE);
                    player = new Player(player_img, new Vector2f(j * TILE_SIZE, i * TILE_SIZE), size, size);
                    player.addAnimation("run", 0, 0, size, size, 8, 80,
                            new Rect(Utils.pixToDip(55), 0, Utils.pixToDip(55), 0), true);
                    player.addAnimation("jump", 0, size, size, size, 2, 10000,
                            new Rect(Utils.pixToDip(55), 0, Utils.pixToDip(55), 0), false);
                    player.addAnimation("slide", 0, 2 * size, size, size, 1, 10000,
                            new Rect(Utils.pixToDip(55), Utils.pixToDip(70), 0, 0), false);

                    player_spawn = new Vector2f(j * TILE_SIZE, i * TILE_SIZE);

                    camera_offset.y -= Utils.pixToDip(size) / 2;
                    camera.setCamera(player_spawn.x - camera_offset.x, player_spawn.y - camera_offset.y);
                }
                else if(r == 127 && g == 127 && b == 127) //GREY = SLIDE UNDER WALL
                {
                    Wall tempw = new Wall(wall_slide_img, new Vector2f(j * TILE_SIZE, i * TILE_SIZE));
                    entities.add(tempw);
                }
                else if(r == 255 && g == 165 && b == 0) //= END DOOR
                {
                    ProgressionDoor temp_pd = new ProgressionDoor(door_img, new Vector2f(j * TILE_SIZE, i * TILE_SIZE));
                    entities.add(temp_pd);
                }
                else if(r == 255 && g == 255 && b == 0)
                {
                    int coin_frames = 10;
                    int coin_w = coin_img.getWidth() / coin_frames;
                    Coin tempc1 = new Coin(coin_img, new Vector2f(j * TILE_SIZE,
                            i * TILE_SIZE + (0.75f * TILE_SIZE - Utils.pixToDip(0.5f * coin_img.getHeight()))),
                            Utils.pixToDip(coin_img.getWidth() / 10), Utils.pixToDip(coin_img.getHeight()));
                    tempc1.addAnimation(coin_w, coin_img.getHeight(), coin_frames, 80);
                    tempc1.setColRect(Utils.pixToDip(20), 0, Utils.pixToDip(10), 0);

                    Coin tempc2 = new Coin(coin_img, new Vector2f(j * TILE_SIZE + Utils.pixToDip(1.25f * coin_w),
                            i * TILE_SIZE + (0.75f * TILE_SIZE - Utils.pixToDip(0.5f * coin_img.getHeight()))),
                            Utils.pixToDip(coin_img.getWidth() / 10), Utils.pixToDip(coin_img.getHeight()));
                    tempc2.addAnimation(coin_w, coin_img.getHeight(), coin_frames, 80);
                    tempc2.setColRect(Utils.pixToDip(20), 0, Utils.pixToDip(10), 0);

                    entities.add(tempc1);
                    entities.add(tempc2);
                }
                else if(r == 200 && g == 200 && b == 0)
                {
                    int coin_frames = 10;
                    int coin_w = coin_img.getWidth() / coin_frames;
                    Coin tempc1 = new Coin(coin_img, new Vector2f(j * TILE_SIZE,
                            i * TILE_SIZE + (0.25f * TILE_SIZE - Utils.pixToDip(0.5f * coin_img.getHeight()))),
                            Utils.pixToDip(coin_img.getWidth() / 10), Utils.pixToDip(coin_img.getHeight()));
                    tempc1.addAnimation(coin_w, coin_img.getHeight(), coin_frames, 80);
                    tempc1.setColRect(Utils.pixToDip(20), 0, Utils.pixToDip(10), 0);

                    Coin tempc2 = new Coin(coin_img, new Vector2f(j * TILE_SIZE + Utils.pixToDip(1.25f * coin_w),
                            i * TILE_SIZE + (0.25f * TILE_SIZE - Utils.pixToDip(0.5f * coin_img.getHeight()))),
                            Utils.pixToDip(coin_img.getWidth() / 10), Utils.pixToDip(coin_img.getHeight()));
                    tempc2.addAnimation(coin_w, coin_img.getHeight(), coin_frames, 80);
                    tempc2.setColRect(Utils.pixToDip(20), 0, Utils.pixToDip(10), 0);

                    Coin tempc3 = new Coin(coin_img, new Vector2f(j * TILE_SIZE,
                            i * TILE_SIZE + (0.75f * TILE_SIZE - Utils.pixToDip(0.5f * coin_img.getHeight()))),
                            Utils.pixToDip(coin_img.getWidth() / 10), Utils.pixToDip(coin_img.getHeight()));
                    tempc3.addAnimation(coin_w, coin_img.getHeight(), coin_frames, 80);
                    tempc3.setColRect(Utils.pixToDip(20), 0, Utils.pixToDip(10), 0);

                    Coin tempc4 = new Coin(coin_img, new Vector2f(j * TILE_SIZE + Utils.pixToDip(1.25f * coin_w),
                            i * TILE_SIZE + (0.75f * TILE_SIZE - Utils.pixToDip(0.5f * coin_img.getHeight()))),
                            Utils.pixToDip(coin_img.getWidth() / 10), Utils.pixToDip(coin_img.getHeight()));
                    tempc4.addAnimation(coin_w, coin_img.getHeight(), coin_frames, 80);
                    tempc4.setColRect(Utils.pixToDip(20), 0, Utils.pixToDip(10), 0);

                    entities.add(tempc1);
                    entities.add(tempc2);
                    entities.add(tempc3);
                    entities.add(tempc4);
                }
            }
        }

    }

    public void onStart()
    {
        //setup sensors
        SensorManager sm = (SensorManager)m_context.getSystemService(Context.SENSOR_SERVICE);
        Sensor gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
        Sensor accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        game_state = State.PLAYING;
    }

    public void onResume()
    {
        onStart();
        if(player != null) game_state = State.PLAYING;
    }

    public void onPause()
    {
        SensorManager sm = (SensorManager)m_context.getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(this);
        StopThread();
        player.setPlaying(false);
    }

    public void onStop()
    {
        SensorManager sm = (SensorManager)m_context.getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(this);
        player.setPlaying(false);
        StopThread();
    }

    public void onDestroy()
    {
        SensorManager sm = (SensorManager)m_context.getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(this);
        player.setPlaying(false);
        Cleanup();
        StopThread();
    }

    public void onRestart() { if(player != null) game_state = State.PLAYING; }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        StopThread();
    }

    private void StartThread()
    {
        if(thread.getState() == Thread.State.TERMINATED)
            thread = new MainThread(getHolder(), this);

        thread.setRunning(true);
        thread.start();
    }

    private void StopThread()
    {
        if(!thread.isAlive()) return;

        boolean retry = true;
        int counter = 0;

        //safely stop game loop
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
        //safely start game loop
        StartThread();
        game_state = State.PLAYING;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(game_state.equals(State.LERP_CAMERA) || game_state.equals(State.COOLDOWN)) return false;
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                //start the game
                if(!player.getPlaying() && !game_state.equals(State.SCANNING_ENVIRONMENT)) player.resume();

                if(game_state.equals(State.SCANNING_ENVIRONMENT)) stopScanning();

                //for jumping/sliding
                downCoords.x = Utils.pixToDip(event.getX());
                downCoords.y = Utils.pixToDip(event.getY());
                swipeTime.start();

                //for starting gyroscope
                startScanningCheck();

                return true;
            case MotionEvent.ACTION_UP:
                upCoords.x = Utils.pixToDip(event.getX());
                upCoords.y = Utils.pixToDip(event.getY());

                checkJumpSlide();

                return true;
        }

        return super.onTouchEvent(event);
    }

    private void checkJumpSlide()
    {
        float xdif = upCoords.x - downCoords.x;
        float ydif = upCoords.y - downCoords.y;

        switch (orientation)
        {
            case LANDSCAPE:
                if(Math.abs(ydif) > Math.abs(xdif) && Math.abs(ydif) > SWIPE_DISTANCE_THRESHOLD && swipeTime.elapsed() < SWIPE_TIME_THRESHOLD)
                {
                    if(game_state.equals(State.SCANNING_ENVIRONMENT))
                    {
                        stopScanning();
                        tappedOnce = false;
                    }

                    if(ydif < 0) player.Jump();
                    else player.Slide();
                }
                break;
            case REVERSE_LANDSCAPE:
                if(Math.abs(ydif) > Math.abs(xdif) && Math.abs(ydif) > SWIPE_DISTANCE_THRESHOLD && swipeTime.elapsed() < SWIPE_TIME_THRESHOLD)
                {
                    if(game_state.equals(State.SCANNING_ENVIRONMENT))
                    {
                        stopScanning();
                        tappedOnce = false;
                    }

                    if(ydif > 0) player.Jump();
                    else player.Slide();
                }
                break;
            case PORTRAIT:
                if(Math.abs(xdif) > Math.abs(ydif) && Math.abs(xdif) > SWIPE_DISTANCE_THRESHOLD && swipeTime.elapsed() < SWIPE_TIME_THRESHOLD)
                {
                    if(game_state.equals(State.SCANNING_ENVIRONMENT))
                    {
                        stopScanning();
                        tappedOnce = false;
                    }

                    if(xdif < 0) player.Jump();
                    else player.Slide();
                }
                break;
            case REVERSE_PORTRAIT:
                if(Math.abs(xdif) > Math.abs(ydif) && Math.abs(xdif) > SWIPE_DISTANCE_THRESHOLD && swipeTime.elapsed() < SWIPE_TIME_THRESHOLD)
                {
                    if(game_state.equals(State.SCANNING_ENVIRONMENT))
                    {
                        stopScanning();
                        tappedOnce = false;
                    }

                    if(xdif > 0) player.Jump();
                    else player.Slide();
                }
                break;
        }
    }

    //check whether to start scanning environment
    private void startScanningCheck()
    {
        if(game_state.equals(State.SCANNING_ENVIRONMENT)) return;

        if(tappedOnce && doubleTapTime.elapsed() < DOUBLE_TAP_TIME_THRESHOLD && player.getAlive())
        {
            tappedOnce = false;
            player.pause();
            game_state = State.SCANNING_ENVIRONMENT;
            scanningTime.start();
        }
        else
        {
            tappedOnce = true;
            doubleTapTime.start();
        }
    }

    public void update()
    {
        //update hiscores on level completion and close level
        if(player.isQuit())
        {
            Hiscores.AddNewScore(num_coins);
            Cleanup();
            ((Activity)m_context).finish();
        }

        switch (game_state)
        {
            case NONE:
                break;
            case PLAYING:
                if(player.getPlaying())
                {
                    CheckAccelerometerChanges();
                    player.update();

                    //check collisions
                    if(player.getAlive())
                    {
                        camera.setCamera(player.getX() - camera_offset.x, player.getY() - camera_offset.y);
                        checkCollisions();
                    }
                }
                break;
            case SCANNING_ENVIRONMENT: //move camera based on gyro values
                if(scanningTime.elapsed() > SCAN_ENV_TIME)
                {
                    stopScanning();
                }
                camera.startScanning(player.getPos());
                camera.MoveScanning(gyroX * -25.f, gyroY * 25);
                break;
            case LERP_CAMERA: //move camera from gyro position back to the player
                if(!camera_transition_step.is_running)
                {
                    camera_transition_step.start();
                }
                else if(camera_transition_step.elapsed() > 1000)
                {
                    Cooldown(1000);
                    break;
                }
                Vector2f new_cam_pos = lerp(start_camera_pos, target_camera_pos, camera_transition_step.elapsed() / 1000f);
                camera.setCamera(new_cam_pos.x, new_cam_pos.y);
                break;
            case LERP_ANGLE: //lerp angle when rotating phone for visual effect
                if(angle_lerp_timer.elapsed() >= 200)
                {
                    angle = goal_angle;
                    angle_lerp_timer.stop();
                    game_state = State.PLAYING;
                    break;
                }
                float ANGLE_LERP_TIMER_THRESHOLD = 500f;
                angle = lerpNum(prev_angle, goal_angle, angle_lerp_timer.elapsed() / ANGLE_LERP_TIMER_THRESHOLD);

                break;
            case COOLDOWN:
                //return to playing again once cooldown timer is up
                if(cooldown_timer.elapsed() > time_to_cooldown)
                {
                    cooldown_timer.stop();
                    game_state = State.PLAYING;
                    player.resume();
                    camera_transition_step.stop();
                }
                break;
        }
        for(GameObject obj : entities)
        {
            if(obj.isAlive())
                obj.update();
        }
    }

    private float lerpNum(float prev_angle, float goal_angle, float v)
    {
        return prev_angle + (goal_angle - prev_angle) * v;
    }

    private void CheckAccelerometerChanges()
    {
        //if values uninitialised
        if(prev_accelX == 0 && prev_accelY == 0 && prev_accelZ == 0)
        {
            prev_accelX = new_accelX;
            prev_accelY = new_accelY;
            prev_accelZ = new_accelZ;
            return;
        }


        if((int)new_accelY == 0)
        {
            if((int)new_accelX > 0) //landscape
            {
                if(orientation == Orientation.LANDSCAPE) return;

                if(orientation_last_frame != Orientation.LANDSCAPE)
                {
                    orientation_last_frame = Orientation.LANDSCAPE;
                    accelerometer_rotate_time.stop();
                    accelerometer_rotate_time.start();
                }
                else if(accelerometer_rotate_time.elapsed() > 100)
                {
                    //player.offsetSlightly();
                    orientation = Orientation.LANDSCAPE;
                    angle_lerp_timer.start();
                    goal_angle = 0;
                    game_state = State.LERP_ANGLE;
                    camera_translation_offset.x = 0;
                    camera_translation_offset.y = 0;
                    SWIPE_DISTANCE_THRESHOLD = HEIGHT / 5;
                }
            }
            else //reverse landscape
            {
                if(orientation == Orientation.REVERSE_LANDSCAPE) return;

                if(orientation_last_frame != Orientation.REVERSE_LANDSCAPE)
                {
                    orientation_last_frame = Orientation.REVERSE_LANDSCAPE;
                    accelerometer_rotate_time.stop();
                    accelerometer_rotate_time.start();
                }
                else if(accelerometer_rotate_time.elapsed() > 100)
                {
                    orientation = Orientation.REVERSE_LANDSCAPE;
                    angle_lerp_timer.start();
                    goal_angle = 0;
                    game_state = State.LERP_ANGLE;
                    camera_translation_offset.x = Utils.dipToPix(WIDTH - WIDTH / 4);
                    camera_translation_offset.y = 0;
                    SWIPE_DISTANCE_THRESHOLD = HEIGHT / 5;
                }
            }

        }
        else if((int)new_accelX == 0)
        {
            if((int)new_accelY > 0) //Portrait
            {
                if(orientation == Orientation.PORTRAIT) return;

                if(orientation_last_frame != Orientation.PORTRAIT)
                {
                    orientation_last_frame = Orientation.PORTRAIT;
                    accelerometer_rotate_time.stop();
                    accelerometer_rotate_time.start();
                }
                else if(accelerometer_rotate_time.elapsed() > 100)
                {
                    orientation = Orientation.PORTRAIT;
                    angle_lerp_timer.start();
                    goal_angle = 180;
                    game_state = State.LERP_ANGLE;
                    //angle = 180;
                    camera_translation_offset.x = Utils.dipToPix(330);
                    camera_translation_offset.y = Utils.dipToPix(100);
                    SWIPE_DISTANCE_THRESHOLD = WIDTH / 5;
                }
            }
            else //Reverse portrait
            {
                if(orientation == Orientation.REVERSE_PORTRAIT) return;

                if(orientation_last_frame != Orientation.REVERSE_PORTRAIT)
                {
                    orientation_last_frame = Orientation.REVERSE_PORTRAIT;
                    accelerometer_rotate_time.stop();
                    accelerometer_rotate_time.start();
                }
                else if(accelerometer_rotate_time.elapsed() > 100)
                {
                    orientation = Orientation.REVERSE_PORTRAIT;
                    angle_lerp_timer.start();
                    goal_angle = 180;
                    game_state = State.LERP_ANGLE;
                    camera_translation_offset.x = -Utils.dipToPix(330);
                    camera_translation_offset.y = -Utils.dipToPix(100);
                    SWIPE_DISTANCE_THRESHOLD = WIDTH / 5;
                }
            }
        }
    }

    //free memory by recycling bitmaps and destroying entities
    private void Cleanup()
    {
        for(GameObject obj : entities)
        {
            obj.Destroy();
        }

        if(gyro_scan_img != null)
            gyro_scan_img.recycle();
        entities.clear();

    }

    private void Cooldown(int time)
    {
        time_to_cooldown = time;
        cooldown_timer.start();
        game_state = State.COOLDOWN;
    }

    //linear interpolation of 2 vectors
    private Vector2f lerp(Vector2f start, Vector2f goal, float step)
    {
        return start.Add((goal.Subtract(start).Multiply(step)));
    }

    //stop scanning the environment and start moving the camera back to the player
    private void stopScanning()
    {
        game_state = State.LERP_CAMERA;
        target_camera_pos = new Vector2f(player.getX() - camera_offset.x, player.getY() - camera_offset.y);
        start_camera_pos.setEqual(camera.getPos());
    }

    @Override
    public void draw(Canvas canvas)
    {
        if(canvas != null)
        {
            super.draw(canvas);
            //translate and rotate the camera to correct position before rendering
            canvas.translate(camera_translation_offset.x, camera_translation_offset.y);
            canvas.rotate(angle, canvas.getWidth() / 2, canvas.getHeight() / 2);

            //if angle has been changed, update camera
            if(goal_angle != prev_angle && game_state != State.LERP_ANGLE)
            {
                camera.setCamera(player.getX() - camera_offset.x, player.getY() - camera_offset.y);
                prev_angle = angle;
            }

            //reset canvas to yellow
            canvas.drawColor(Color.YELLOW);

            for(GameObject obj : entities)
            {
                if(obj.isAlive()) obj.draw(canvas);
            }

            //draw the black blurred image when scanning the environment
            if(game_state.equals(State.LERP_CAMERA) || game_state.equals(State.SCANNING_ENVIRONMENT))
            {
                canvas.drawBitmap(gyro_scan_img, Utils.dipToPix(camera.getScanRect().left - camera.getPos().x - 2),
                        Utils.dipToPix(camera.getScanRect().top - camera.getPos().y - 2), null);
            }

            player.draw(canvas);
        }
    }

    public void checkCollisions()
    {
        for(GameObject obj : entities)
        {
            if(obj.isAlive())
                player.checkCollision(obj);
        }

        player.collisionCheckComplete();
    }

    //Reset the map when the player dies
    public static void Reset()
    {
        player.setPos(player_spawn);
        for(GameObject obj : entities)
        {
            if(obj.tag.equals("coin"))
            {
                obj.setAlive(true);
                obj.Reset();
            }
        }
        num_coins = 0;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //update gyroscope values only when we are scanning environment
        if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE && game_state.equals(State.SCANNING_ENVIRONMENT))
        {
            gyroX = sensorEvent.values[0];
            gyroY = sensorEvent.values[1];
        }
        //update accelerometer values providing we aren't scanning the environment
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !game_state.equals(State.SCANNING_ENVIRONMENT))
        {
            new_accelX = sensorEvent.values[0];
            new_accelY = sensorEvent.values[1];
            new_accelZ = sensorEvent.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public static void incrementCoins()
    {
        num_coins += 10;
        System.out.println(num_coins);
    }
}
