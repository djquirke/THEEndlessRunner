package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Rect;

public class Camera {
    private Vector2f pos;
    private int width, height, map_height, map_width;
    private Rect rect;

    public Vector2f getPos() {return pos;}
    public Rect getRect() {return rect;}

    protected Camera(float x, float y, int width, int height)
    {
        pos = new Vector2f(x, y);
        this.width = width;
        this.height = height;
        this.map_height = height;
        this.map_width = width;
        rect = new Rect((int)x, (int)y, width, height);
    }

    public void setCamera(float xpos, float ypos)
    {
        if(xpos < 0) xpos = 0;
        if(ypos < 0) ypos = 0;
        if(ypos > map_height - height) ypos = map_height - height;
        if(xpos > map_width - width) xpos = map_width - width;

        pos.x = xpos;
        pos.y = ypos;

        updateRect();
    }

    public void setMapSize(Vector2f size)
    {
        this.map_width = (int) size.x;
        this.map_height = (int)size.y;
    }
    public void setMapSize(int width, int height)
    {
        this.map_width = width;
        this.map_height = height;
    }
    public void setMapHeight(int height)
    {
        this.map_height = height;
    }
    public void setMapWidth(int width)
    {
        this.map_width = width;
    }

    public void Move(float x, float y)
    {
        this.pos.x += x;
        this.pos.y += y;

        if(pos.x < 0) pos.x = 0;
        if(pos.y < 0) pos.y = 0;
        if(pos.y > map_height - height) pos.y = map_height - height;
        if(pos.x > map_width - width) pos.x = map_width - width;

        updateRect();
    }

    private void updateRect()
    {
        rect.set((int)pos.x, (int)pos.y, (int)pos.x + width, (int)pos.y + height);
    }
}
