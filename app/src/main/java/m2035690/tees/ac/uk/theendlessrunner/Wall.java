package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Wall extends GameObject {
    Bitmap image;

    @Override
    public void Destroy()
    {
        image.recycle();
    }

    public Wall(Bitmap image, Vector2f pos)
    {
        this.image = image;
        this.pos = pos;
        this.height = Utils.pixToDip(image.getHeight());
        this.width = Utils.pixToDip(image.getWidth());
        this.tag = "wall";
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
