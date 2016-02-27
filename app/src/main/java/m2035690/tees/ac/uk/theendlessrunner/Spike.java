package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Dan on 27/02/2016.
 */
public class Spike extends GameObject {
    Bitmap image;
    private int damage;

    public Spike(Bitmap image, Vector2f pos, int damage)
    {
        this.image = image;
        this.damage = damage;
        this.pos = pos;
        this.height = (int)Utils.pixToDip((float)image.getHeight());
        this.width = (int)Utils.pixToDip((float)image.getWidth());
    }

    @Override
    public void draw(Canvas canvas)
    {
        if(this.getRect().intersect(GamePanel.camera.getDims()))
        {
//            Paint p = new Paint(Color.BLUE);
//            canvas.drawRect(Utils.dipToPix(getColRect().left), Utils.dipToPix(getColRect().top),
//                    Utils.dipToPix(getColRect().right), Utils.dipToPix(getColRect().bottom), p);

            Vector2f campos = GamePanel.camera.getPos();
            canvas.drawBitmap(image, Utils.dipToPix(pos.x - campos.x),
                    Utils.dipToPix(pos.y - campos.y), null);
        }
    }
}
