package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Rect;

public class Camera {
    private Vector2f pos;
    private int width, height, map_height, map_width;
    private Rect rect;
    private Rect scan_rect;

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
        scan_rect = new Rect();
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

    private void updateRect()
    {
        rect.set((int) pos.x, (int) pos.y, (int) pos.x + width, (int) pos.y + height);
    }

    public void MoveScanning(float x, float y)
    {
        this.pos.x += x;
        this.pos.y += y;

        if(pos.x < scan_rect.left) pos.x = scan_rect.left;
        if(pos.y < scan_rect.top) pos.y = scan_rect.top;
        if(pos.y > scan_rect.bottom - height) pos.y = scan_rect.bottom - height;
        if(pos.x > scan_rect.right - width) pos.x = scan_rect.right - width;

        updateRect();
    }

    public Rect getScanRect() {return scan_rect;}

    public void InitialiseScanRect()
    {
        Rect temp = new Rect();
        temp.left = -width;
        temp.top = -width;
        temp.right = width;
        temp.bottom = width;

        scan_rect.set(temp);
    }

    public void startScanning(Vector2f pos)
    {
        int x = (int)pos.x;
        int y = (int)pos.y;

        Rect temp = new Rect();
        temp.left = x - width;
        temp.top = y - width;
        temp.right = x + width;
        temp.bottom = y + width;

        if(temp.left < 0) {temp.right += Math.abs(temp.left); temp.left = 1;}
        if(temp.top < 0) {temp.bottom += Math.abs(temp.top); temp.top = 1;}
        if(temp.right > map_width) {temp.left -= (temp.right - map_width); temp.right = map_width - 1;}
        if(temp.bottom > map_height) {temp.top -= (temp.bottom - map_height); temp.bottom = map_height - 1;}
        scan_rect.set(temp);
    }
}
