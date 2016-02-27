package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Created by Dan on 24/02/2016.
 */
public class GameObject {
    protected Vector2f pos;
    protected float dx, dy;
    protected int width, height;
    protected Rect collisionRect = new Rect();

    public void setX(int x) {this.pos.x = x;}
    public void setY(int y) {this.pos.y = y;}
    public void setPos(Vector2f pos) {this.pos = pos;}
    public void setPos(float x, float y) {this.pos.x = x; this.pos.y = y;}
    public float getX() {return pos.x;}
    public float getY() {return pos.y;}
    public Vector2f getPos() {return pos;}
    public int getWidth() {return width;}
    public int getHeight() {return height;}

    public Rect getRect()
    {
        int x2 = (int)pos.x;
        int y2 = (int)pos.y;
        return new Rect(x2, y2, x2 + width, y2 + height);
    }

    public Rect getColRect()
    {
        Rect outer = getRect();
        return new Rect(outer.left + collisionRect.left, outer.top + collisionRect.top,
                        outer.right - collisionRect.right, outer.bottom - collisionRect.bottom);
    }

    public void setColRect(Rect rect) {collisionRect = rect;}
    public void setColRect(int left, int top, int right, int bottom)
    {
        collisionRect.left = left;
        collisionRect.top = top;
        collisionRect.right = right;
        collisionRect.bottom = bottom;
    }

    public void update() {}
    public void draw(Canvas canvas) {}
}
