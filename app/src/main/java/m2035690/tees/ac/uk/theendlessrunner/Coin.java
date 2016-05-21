package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;


public class Coin extends GameObject {
    Bitmap spritesheet;
    Animation animation;

    public Coin(Bitmap image, Vector2f pos, float width, float height)
    {
        this.spritesheet = image;
        this.pos = pos;
        this.height = (int)height;
        this.width = (int)width;
        this.tag = "coin";
    }

    @Override
    public void Destroy()
    {
        spritesheet.recycle();
    }


    @Override
    public void update() {animation.update();}

    @Override
    public void draw(Canvas canvas)
    {
        //if(this.getRect().intersect(GamePanel.camera.getRect()))
        {
            Vector2f campos = GamePanel.camera.getPos();
            canvas.drawBitmap(animation.getImage(), Utils.dipToPix(pos.x - campos.x),
                    Utils.dipToPix(pos.y - campos.y), null);
        }
    }

    @Override
    public void Reset() {animation.setFrame(0);}

    public void addAnimation(int frame_w, int frame_h, int numFrames, int delay)
    {
        Bitmap[] image = new Bitmap[numFrames];

        for(int i = 0; i < numFrames; i++)
        {
            image[i] = Bitmap.createBitmap(spritesheet, i * frame_w, 0, frame_w, frame_h);
        }

        animation = new Animation();
        animation.setFrames(image);
        animation.setDelay(delay);
    }
}
