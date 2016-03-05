package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class GameObject {
    protected Vector2f pos;
    protected int width, height;
    protected Rect collisionRect = new Rect();
    protected String tag;

    public void setX(int x) {this.pos.x = x;}
    public void setY(int y) {this.pos.y = y;}
    public void setPos(Vector2f pos) {this.pos.x = pos.x; this.pos.y = pos.y;}
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
    public void drawDebug(Canvas canvas, int colRectCol)
    {
        Vector2f campos = GamePanel.camera.getPos();
        Paint p = new Paint();
        p.setColor(colRectCol);
        p.setAlpha(200);
        canvas.drawRect(Utils.dipToPix(getColRect().left - campos.x), Utils.dipToPix(getColRect().top - campos.y),
                Utils.dipToPix(getColRect().right - campos.x), Utils.dipToPix(getColRect().bottom - campos.y),
                p);
    }
}
