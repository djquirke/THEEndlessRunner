package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
    private HashMap<String, Animation> animations = new HashMap<String, Animation>();
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

    private boolean quit;

    @Override
    public void Destroy()
    {
        //if(!spritesheet.isRecycled())
            spritesheet.recycle();
    }

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
        quit = false;
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

        switch (GamePanel.orientation)
        {
            case LANDSCAPE:
                rotation_angle = 0;
                pos.x += moveSpeed;
                pos.y += GRAVITY;

                if(jump)
                {
                    pos.y -= GRAVITY;
                    pos.y += velocity;
                    velocity += VELOCITY_GRAVITY;

                    if(velocity >= 0) getAnimation().setFrame(1);
                }
                break;
            case REVERSE_LANDSCAPE:
                rotation_angle = 180;
                pos.x -= moveSpeed;
                pos.y -= GRAVITY;

                if(jump)
                {
                    pos.y += GRAVITY;
                    pos.y -= velocity;
                    velocity += VELOCITY_GRAVITY;

                    if(velocity >= 0) getAnimation().setFrame(1);
                }
                break;
            case PORTRAIT:
                rotation_angle = 90;
                pos.x -= GRAVITY;
                pos.y += moveSpeed;

                if(jump)
                {
                    pos.x += GRAVITY;
                    pos.x -= velocity;
                    velocity += VELOCITY_GRAVITY;

                    if(velocity >= 0) getAnimation().setFrame(1);
                }
                break;
            case REVERSE_PORTRAIT:
                rotation_angle = 270;
                pos.x += GRAVITY;
                pos.y -= moveSpeed;

                if(jump)
                {
                    pos.x -= GRAVITY;
                    pos.x += velocity;
                    velocity += VELOCITY_GRAVITY;

                    if(velocity >= 0) getAnimation().setFrame(1);
                }
                break;
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

        Matrix m = new Matrix();

        switch (GamePanel.orientation)
        {
            case LANDSCAPE:
                m.postRotate(0, getAnimation().getImage().getWidth() / 2, getAnimation().getImage().getHeight() / 2);

                break;
            case REVERSE_LANDSCAPE:
                m.postRotate(180, getAnimation().getImage().getWidth() / 2, getAnimation().getImage().getHeight() / 2);
                break;
            case PORTRAIT:
                m.postRotate(90, getAnimation().getImage().getWidth() / 2, getAnimation().getImage().getHeight() / 2);
                break;
            case REVERSE_PORTRAIT:
                m.postRotate(270, getAnimation().getImage().getWidth() / 2, getAnimation().getImage().getHeight() / 2);
                break;
        }

        m.postTranslate(Utils.dipToPix(pos.x - campos.x), Utils.dipToPix(pos.y - campos.y));

        canvas.drawBitmap(getAnimation().getImage(), m, null);
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

                switch (GamePanel.orientation)
                {
                    case LANDSCAPE:
                        if(colRect.top == playerRect.top) Die(); //Headbutt
                        else if(colRect.top > playerRect.top)
                        {
                            int colRectHeight = Math.abs(colRect.bottom - colRect.top);

                            if(colRectHeight < this.height / 2)
                            {
                                pos.y -= colRectHeight;//prev_pos.y;
                            }
                            else
                            {
                                Die();
                            }
                        }
                        break;
                    case REVERSE_LANDSCAPE:
                        if(colRect.bottom == playerRect.bottom) Die(); //Headbutt
                        else if(colRect.bottom < playerRect.bottom)
                        {
                            int colRectHeight = Math.abs(colRect.bottom - colRect.top);

                            if(colRectHeight < this.height / 2)
                            {
                                pos.y += colRectHeight;//prev_pos.y;
                            }
                            else
                            {
                                Die();
                            }
                        }
                        break;
                    case PORTRAIT:
                        if(colRect.right == playerRect.right) Die(); //Headbutt
                        else if(colRect.right < playerRect.right)
                        {
                            int colRectHeight = Math.abs(colRect.left - colRect.right);

                            if(colRectHeight < this.width / 2)
                            {
                                pos.x += colRectHeight;//prev_pos.y;
                            }
                            else
                            {
                                Die();
                            }
                        }
                        break;
                    case REVERSE_PORTRAIT:
                        if(colRect.left == playerRect.left) Die(); //Headbutt
                        else if(colRect.left > playerRect.left)
                        {
                            int colRectHeight = Math.abs(colRect.left - colRect.right);

                            if(colRectHeight < this.width / 2)
                            {
                                pos.x -= colRectHeight;//prev_pos.y;
                            }
                            else
                            {
                                Die();
                            }
                        }
                        break;
                }

            }
            else if(other.tag.equals("prog_door"))
            {
                quit = true;
                //GamePanel.Quit();
                //System.out.println("hit end door");
            }
            else if(other.tag.equals("coin"))
            {
                GamePanel.incrementCoins();
                other.setAlive(false);
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

    public boolean isQuit() {return quit;}

}
