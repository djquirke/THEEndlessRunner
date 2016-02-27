package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

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
    private static final long SLIDE_TIME = 500;
    private float velocity;
    private boolean jump, slide, playing;
    private HashMap<String, Animation> animations = new HashMap<>();
    private String currentAnimation;
    private Stopwatch slideRunTime = new Stopwatch();

    public Player(Bitmap res, Vector2f pos, int w, int h)
    {
        this.pos = new Vector2f(pos);
        moveSpeed = 7;
        height = Utils.pixToDip(h);
        width = Utils.pixToDip(w);

        spritesheet = res;
    }

    public void addAnimation(String name, int xpos, int ypos, int frame_w, int frame_h, int numFrames, int delay, Rect colRect, boolean setActive)
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
        temp.setColRect(colRect);

        animations.put(name, temp);
        if(setActive) {currentAnimation = name; collisionRect = colRect;}
    }

    public void Jump()
    {
        if(jump) return;

        jump = true;
        slide = false;
        velocity = JUMP_VELOCITY;
        setAnimation("jump");
    }

    public void Slide()
    {
        if(slide || jump) return;

        slide = true;
        setAnimation("slide");
        slideRunTime.start();
    }

    @Override
    public void update()
    {
        getAnimation().update();

        pos.x += moveSpeed;
        //System.out.println("player pos:" + pos.x + " " + pos.y);

        if(jump)
        {
            pos.y += velocity;
            velocity += GRAVITY;

            if(velocity >= 0) getAnimation().setFrame(1);
            if(GamePanel.checkCollision()) setPlaying(false);

            if(pos.y > GamePanel.player_offset.y)
            {
                pos.y = GamePanel.player_offset.y;
                jump = false;
                velocity = 0;
                setAnimation("run");
            }
        }

        if(slide)
        {
            if(slideRunTime.elapsed() > SLIDE_TIME)
            {
                slide = false;
                setAnimation("run");
            }
        }
    }

    @Override
    public void draw(Canvas canvas)
    {
        Vector2f campos = GamePanel.camera.getPos();

        Paint p = new Paint(Color.BLUE);
        canvas.drawRect(Utils.dipToPix(getColRect().left - campos.x), Utils.dipToPix(getColRect().top - campos.y),
                Utils.dipToPix(getColRect().right - campos.x), Utils.dipToPix(getColRect().bottom - campos.y),
                p);

        canvas.drawBitmap(getAnimation().getImage(), Utils.dipToPix(pos.x - campos.x),
                Utils.dipToPix(pos.y - campos.y), null);
    }

    private void setAnimation(String key)
    {
        currentAnimation = key;
        collisionRect = getAnimation().getColRect();
        getAnimation().setFrame(0);
    }

    public int getScore() {return score;}
    public void resetScore() {score = 0;}
    public boolean getPlaying() {return playing;}
    public void setPlaying(boolean b) {playing = b;}
    private Animation getAnimation() {return animations.get(currentAnimation);}
}
