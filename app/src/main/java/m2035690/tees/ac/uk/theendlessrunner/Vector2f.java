package m2035690.tees.ac.uk.theendlessrunner;

/**
 * Created by Dan on 26/02/2016.
 */
public class Vector2f {
    float x, y;

    public Vector2f()
    {
        x = 0;
        y = 0;
    }

    public Vector2f(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Vector2f other)
    {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2f Add(Vector2f other)
    {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    public Vector2f Subtract(Vector2f other)
    {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    public Vector2f Multiply(Vector2f other)
    {
        return new Vector2f(this.x * other.x, this.y * other.y);
    }

    public Vector2f Divide(Vector2f other)
    {
        return new Vector2f(this.x / other.x, this.y / other.y);
    }

    public Vector2f AddEquals(Vector2f other)
    {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vector2f SubtractEquals(Vector2f other)
    {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Vector2f MultiplyEquals(Vector2f other)
    {
        this.x *= other.x;
        this.y *= other.y;
        return this;
    }

    public Vector2f DivideEquals(Vector2f other)
    {
        this.x /= other.x;
        this.y /= other.y;
        return this;
    }

    public void setEqual(Vector2f other)
    {
        this.x = other.x;
        this.y = other.y;
    }

    @Override
    public boolean equals(Object other)
    {
        Vector2f obj = (Vector2f)other;
        return (this.x == obj.x && this.y == obj.y);
    }
}
