package m2035690.tees.ac.uk.theendlessrunner;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class GameObject {
    protected Vector2f pos;
    protected int width, height;
    protected Rect collisionRect = new Rect();
    protected String tag;
    protected boolean isAlive = true;
    protected int rotation_angle = 0;

    public void setPos(Vector2f pos) {this.pos.x = pos.x; this.pos.y = pos.y;}
    public float getX() {return pos.x;}
    public float getY() {return pos.y;}
    public Vector2f getPos() {return pos;}
    public int getWidth() {return width;}
    public int getHeight() {return height;}
    public boolean isAlive() {return isAlive;}
    public void setAlive(boolean value) {isAlive = value;}

    public Rect getRect()
    {
        int x2 = (int)pos.x;
        int y2 = (int)pos.y;
        return new Rect(x2, y2, x2 + width, y2 + height);
    }

    public Rect getColRect()
    {
        Rect outer = getRect();
        Rect temp = new Rect(outer.left + collisionRect.left, outer.top + collisionRect.top,
                             outer.right - collisionRect.right, outer.bottom - collisionRect.bottom);

        if(rotation_angle == 0) return temp;

        Matrix m = new Matrix();
        m.setRotate(rotation_angle, temp.centerX(), temp.centerY());
        RectF converted = new RectF(temp);
        m.mapRect(converted);
        temp.set((int)converted.left, (int)converted.top, (int)converted.right, (int)converted.bottom);

        return temp;

    }

    public void setColRect(int left, int top, int right, int bottom)
    {
        collisionRect.left = left;
        collisionRect.top = top;
        collisionRect.right = right;
        collisionRect.bottom = bottom;
    }

    public void update() {}
    public void draw(Canvas canvas) {}
    public void Reset() {}
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


    @Override
    public boolean equals(Object other)
    {
        GameObject obj = (GameObject) other;

        return (this.tag.equals(obj.tag) && this.pos == obj.pos);
    }

    public void Destroy() {}
}
