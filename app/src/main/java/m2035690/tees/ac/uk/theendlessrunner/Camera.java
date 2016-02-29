package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Rect;

/**
 * Created by Dan on 26/02/2016.
 */
public class Camera {
    private Vector2f pos;
    private int width, height;
    private Rect rect;

    public Vector2f getPos() {return pos;}
    public Rect getRect() {return rect;}

    protected Camera(float x, float y, int width, int height)
    {
        pos = new Vector2f(x, y);
        this.width = width;
        this.height = height;
        rect = new Rect((int)x, (int)y, width, height);
    }

    public void setCamera(float xpos, float ypos, int map_width, int map_height)
    {
        if(xpos < 0) xpos = 0;
        if(ypos < 0) ypos = 0;
        //if(ypos > map_height - height) ypos = map_height - height;
       // if(xpos > map_width - width) xpos = map_width - width;

        pos.x = xpos;
        pos.y = ypos;

        //System.out.println("camera pos:" + this.pos.x + " " + this.pos.y);

        rect.set((int)pos.x, (int)pos.y, (int)pos.x + width, (int)pos.y + height);
    }

    public void setCamera(Vector2f pos, int map_width, int map_height)
    {
        if(pos.x < 0) pos.x = 0;
        if(pos.y < 0) pos.y = 0;
        if(pos.y > map_height - height) pos.y = map_height - height;
        if(pos.x > map_width - width) pos.x = map_width - width;

        this.pos.x = pos.x - 100;
        this.pos.y = pos.y - (GamePanel.HEIGHT / 2 - 47.5f);
        //System.out.println(this.pos.x + " " + this.pos.y);
        rect.set((int)this.pos.x, (int)this.pos.y,
                       (int)this.pos.x + width, (int)this.pos.y + height);
    }
}
