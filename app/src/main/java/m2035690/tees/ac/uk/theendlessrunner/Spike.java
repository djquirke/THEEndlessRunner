package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;


public class Spike extends GameObject {
    Bitmap image;

    @Override
    public void Destroy()
    {
        //if(!image.isRecycled())
            image.recycle();
    }

    public Spike(Bitmap image, Vector2f pos)
    {
        this.image = image;
        this.pos = pos;
        this.height = (int)Utils.pixToDip((float)image.getHeight());
        this.width = (int)Utils.pixToDip((float)image.getWidth());
        this.tag = "spike";
    }

    @Override
    public void draw(Canvas canvas)
    {
        //if(this.getRect().intersect(GamePanel.camera.getRect()))
        {
            Vector2f campos = GamePanel.camera.getPos();
            canvas.drawBitmap(image, Utils.dipToPix(pos.x - campos.x),
                    Utils.dipToPix(pos.y - campos.y), null);
        }
    }
}
