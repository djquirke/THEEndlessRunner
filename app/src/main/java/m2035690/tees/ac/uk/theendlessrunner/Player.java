package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.HashMap;


public class Player extends GameObject {
    private static final float JUMP_VELOCITY = -15;
    private static final float VELOCITY_GRAVITY = 1.f;
    private static final float GRAVITY = 10.f;
    private static final long SLIDE_TIME = 750;
    private static final int MOVE_SPEED = 7;
    private static final long DEATH_TIME = 1500;

    private Bitmap spritesheet;
    private HashMap<String, Animation> animations = new HashMap<>();
    private String currentAnimation;
    private int score;
    private int moveSpeed;
    private float velocity;
    private boolean jump, slide, playing;
    private Stopwatch slideRunTime = new Stopwatch();
    private boolean collidedThisFrame, falling;
    private Stopwatch timeDead = new Stopwatch();
    Rect colRect = new Rect();
    private boolean isAlive;

    public Player(Bitmap res, Vector2f pos, int w, int h)
    {
        this.pos = new Vector2f(pos);
        moveSpeed = MOVE_SPEED;
        height = Utils.pixToDip(h);
        width = Utils.pixToDip(w);
        spritesheet = res;
        tag = "player";
        collidedThisFrame = false;
        falling = false;
        isAlive = true;
    }

    public void addAnimation(String name, int xpos, int ypos, int frame_w, int frame_h, int numFrames, int delay, Rect colRect, boolean setActive)
    {
        Bitmap[] image = new Bitmap[numFrames];

        for(int i = 0; i < numFrames; i++)
        {
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
        if(isAlive)
        {
            getAnimation().update();

            if(slide)
            {
                if(slideRunTime.elapsed() > SLIDE_TIME)
                {
                    stopSliding();
                }
            }
        }

        pos.x += moveSpeed;
        pos.y += GRAVITY;

        if(jump)
        {
            pos.y -= GRAVITY;
            pos.y += velocity;
            velocity += VELOCITY_GRAVITY;

            if(velocity >= 0) getAnimation().setFrame(1);
        }

        if(!isAlive && timeDead.elapsed() > DEATH_TIME)
        {
            Restart();
        }

    }

    private void Restart()
    {
        isAlive = true;
        pos.setEqual(GamePanel.player_spawn);
        moveSpeed = MOVE_SPEED;
        stopSliding();
        stopJumping();

        //TODO: remove this when proper loading implemented
        GamePanel.Reset();
    }

    private void stopJumping() {
        jump = false;
        velocity = 0;
        setAnimation("run");
    }

    private void stopSliding()
    {
        slide = false;
        setAnimation("run");
    }

    @Override
    public void draw(Canvas canvas)
    {
        Vector2f campos = GamePanel.camera.getPos();

        canvas.drawBitmap(getAnimation().getImage(), Utils.dipToPix(pos.x - campos.x),
                Utils.dipToPix(pos.y - campos.y), null);
    }

    public void checkCollision(GameObject other)
    {
        if(colRect.setIntersect(other.getColRect(), this.getColRect()) && isAlive)
        {
            if(other.tag.equals("spike"))
            {
                Die();
            }
            else if (other.tag.equals("wall"))
            {
                collidedThisFrame = true;

                if(jump || falling)
                    stopJumping();

                Rect playerRect = getColRect();

                if(colRect.top == playerRect.top) Die();
                else if(colRect.top > playerRect.top)
                {
                    int colRectHeight = colRect.bottom - colRect.top;

                    if(colRectHeight < this.height / 2)
                    {
                        pos.y -= (colRect.bottom - colRect.top);//prev_pos.y;
                    }
                    else
                    {
                        Die();
                    }
                }
            }
        }
    }

    private void Die()
    {
        moveSpeed = 0;
        stopJumping();
        stopSliding();
        Jump();
        timeDead.start();
        isAlive = false;
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

    public void collisionCheckComplete()
    {
        if(!collidedThisFrame && !jump)
        {
            falling = true;
            setAnimation("jump");
            getAnimation().setFrame(1);
        }
        else
        {
            falling = false;
        }
        collidedThisFrame = false;
    }
    public boolean getAlive() {return isAlive;}

    public void pause()
    {
        setPlaying(false);
        if(slide)
        {
            slideRunTime.pause();
        }
    }

    public void resume()
    {
        setPlaying(true);
        if(slide)
        {
            slideRunTime.resume();
        }
    }
}
