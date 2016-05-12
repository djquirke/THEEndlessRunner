package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Rect;

public class Camera {
    private Vector2f pos;
    private int width, height, map_height, map_width;
    private Rect rect;
    private Rect scan_rect;

    public Vector2f getPos() {return pos;}
    public Rect getRect() {return rect;}
    private float angle = 0;

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
        rect.set((int) pos.x, (int) pos.y, (int) pos.x + width, (int) pos.y + height);
    }

    public void setScanRect(Vector2f player_pos)
    {
        int x = (int)player_pos.x;
        int y = (int)player_pos.y;

        Rect temp = new Rect();
        temp.left = x - width;
        temp.top = y - height;
        temp.right = x + width;
        temp.bottom = y + height;

        if(temp.left < 0) {temp.right += Math.abs(temp.left); temp.left = 1;}
        if(temp.top < 0) {temp.bottom += Math.abs(temp.top); temp.top = 1;}
        if(temp.right > map_width) {temp.left -= (temp.right - map_width); temp.right = map_width - 1;}
        if(temp.bottom > map_height) {temp.top -= (temp.bottom - map_height); temp.bottom = map_height - 1;}
        scan_rect.set(temp);
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

//        if(temp.left < 0) {temp.right += Math.abs(temp.left); temp.left = 1;}
//        if(temp.top < 0) {temp.bottom += Math.abs(temp.top); temp.top = 1;}
//        if(temp.right > map_width) {temp.left -= (temp.right - map_width); temp.right = map_width - 1;}
//        if(temp.bottom > map_height) {temp.top -= (temp.bottom - map_height); temp.bottom = map_height - 1;}
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

    public void setAngle(float angle)
    {
        if(this.angle == angle || (this.angle + 180) == angle) return;
        int temp_w = width;
        width = height;
        height = temp_w;

        float tempX = rect.left - rect.centerX();
        float tempY = rect.bottom - rect.centerY();

        float dis_to_rotate = angle - this.angle;

        double rotX = tempX * Math.cos(dis_to_rotate) - tempY * Math.sin(dis_to_rotate);
        double rotY = tempX * Math.sin(dis_to_rotate) + tempY * Math.cos(dis_to_rotate);

        float tempX2 = rect.right - rect.centerX();
        float tempY2 = rect.top - rect.centerY();

        double rotX2 = tempX2 * Math.cos(dis_to_rotate) - tempY2 * Math.sin(dis_to_rotate);
        double rotY2 = tempX2 * Math.sin(dis_to_rotate) + tempY2 * Math.cos(dis_to_rotate);


        rect.left = (int)rotX;
        rect.top = (int)rotY;
        rect.right = (int)rotX2;
        rect.bottom = (int)rotY2;

        this.angle = angle;
    }

    public void rotate(float angle)
    {
        System.out.println("RECTANGLE BEFORE ROTATION: " + rect);//.left + " " + rect.top + " " + rect.right + " " + rect.bottom);
        if(Math.abs(angle - this.angle) % 180 == 0)
        {
            int temp_r = rect.right;
            int temp_b = rect.bottom;
            rect.right = rect.left;
            rect.bottom = rect.top;
            rect.left = temp_r;
            rect.bottom = temp_b;
        }
        else
        {
            rect.left += ((width - height) / 2);
            rect.top -= ((width - height) / 2);
            rect.right -= ((width - height) / 2);
            rect.bottom += ((width - height) / 2);

            int temp_w = width;
            width = height;
            height = temp_w;
        }
        System.out.println("RECTANGLE AFTER ROTATION: " + rect);
        this.angle = angle;

    }

    public Vector2f translate(Vector2f translation)
    {
        Vector2f temp_pos = new Vector2f(pos.x, pos.y);
        temp_pos.x -= translation.x;
        temp_pos.y += translation.y;

        if(temp_pos.x < 0) temp_pos.x = pos.x;
        else if(temp_pos.x > map_width - width) temp_pos.x = (temp_pos.x + width) - map_width;
        else temp_pos.x = translation.x;
        if(temp_pos.y < 0) temp_pos.y = -pos.y;
        else if(temp_pos.y > map_height - height) temp_pos.y = -((temp_pos.y + height) - map_height);
        else temp_pos.y = translation.y;

        System.out.println("POS AFTER TRANSLATION: " + temp_pos.x + " " + temp_pos.y);
        return temp_pos;
    }
}
