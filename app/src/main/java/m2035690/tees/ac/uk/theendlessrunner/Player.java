package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.HashMap;

/**
 * Created by Dan on 24/02/2016.
 */
public class Player extends GameObject {
    private Bitmap spritesheet;
    private int score;
    private int moveSpeed;
    private static final float JUMP_VELOCITY = -13;
    private static final float GRAVITY = 1.f;
    private static final int slideMom = 10;
    private float velocity;
    private boolean jump, slide, playing;
    private HashMap<String, Animation> animations = new HashMap<String, Animation>();
    private String currentAnimation;
    private long startTime;

    public Player(Bitmap res, Vector2f pos, int w, int h)
    {
        this.pos = new Vector2f(pos);
        moveSpeed = 7;
        height = (int)Utils.pixToDip(h);
        width = (int)Utils.pixToDip(w);

        spritesheet = res;

        startTime = System.nanoTime();
    }

    public void addAnimation(String name, int xpos, int ypos, int frame_w, int frame_h, int numFrames, int delay, boolean setActive)
    {
        Bitmap[] image = new Bitmap[numFrames];

        for(int i = 0; i < numFrames; i++)
        {
            System.out.println(i * frame_w);
            image[i] = Bitmap.createBitmap(spritesheet, i * frame_w + xpos, ypos, frame_w, frame_h);
        }

        Animation temp = new Animation();
        temp.setFrames(image);
        temp.setDelay(delay);

        animations.put(name, temp);
        if(setActive) {currentAnimation = name;}
    }

    public void Jump()
    {
        if(jump) return;

        jump = true;
        velocity = JUMP_VELOCITY;
        setAnimation("jump");
        getAnimation().setFrame(0);
    }

    @Override
    public void update()
    {
        long elapsed = (System.nanoTime() - startTime) / 1000000;

        getAnimation().update();

        pos.x += moveSpeed;
        //System.out.println("player pos:" + pos.x + " " + pos.y);

        if(jump)
        {
            pos.y += velocity;
            velocity += GRAVITY;

            if(velocity >= 0)
            {
                getAnimation().setFrame(1);
            }

            if(GamePanel.checkCollision())
            {
                setPlaying(false);
            }

            if(pos.y > GamePanel.player_offset.y)
            {
                pos.y = GamePanel.player_offset.y;
                jump = false;
                velocity = 0;
                setAnimation("run");
            }
        }
    }

    @Override
    public void draw(Canvas canvas)
    {
        //if(this.getRect().intersect(GamePanel.camera.getDims()))
        //{
//        Paint p = new Paint(Color.BLUE);
//        canvas.drawRect(Utils.dipToPix(getColRect().left), Utils.dipToPix(getColRect().top),
//                Utils.dipToPix(getColRect().right), Utils.dipToPix(getColRect().bottom), p);

        Vector2f campos = GamePanel.camera.getPos();
        canvas.drawBitmap(getAnimation().getImage(), Utils.dipToPix(pos.x - campos.x),
                Utils.dipToPix(pos.y - campos.y), null);


        //}
    }

    public int getScore() {return score;}
    public void resetScore() {score = 0;}
    public boolean getPlaying() {return playing;}
    public void setPlaying(boolean b) {playing = b;}
    private void setAnimation(String key) {currentAnimation = key;}
    private Animation getAnimation() {return animations.get(currentAnimation);}
}
