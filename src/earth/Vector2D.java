package earth;

public record Vector2D(double x, double y) {

    Vector2D(double angle) {
        this(Math.cos(angle), -Math.sin(angle));
    }

    public Vector2D add(Vector2D b) {
        return new Vector2D(x + b.x, y + b.y);
    }

    public Vector2D subtract(Vector2D b) {
        return new Vector2D(x - b.x, y - b.y);
    }

    /**
     * Returns a new vector representing the current vector rotated anti-clockwise by angle radians
     * @param angle the angle to rotate in radians
     * @return the new rotated vector
     */
    public Vector2D rotate(double angle) {
        return new Vector2D(Math.cos(angle) * x - Math.sin(angle) * y, Math.sin(angle) * x + Math.cos(angle) * y);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2D scale(double scale) {
        return new Vector2D(scale * x, scale * y);
    }

    public Vector2D scaleTo(double scale) {
        double magnitude = magnitude();
        return new Vector2D(scale * x / magnitude, scale * y / magnitude);
    }

    public double dot(Vector2D b) {
        return x * b.x + y * b.y;
    }
}
