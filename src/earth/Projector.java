package earth;

public class Projector {

    private final int width;
    private final int height;
    private final double near;
    private final double far;
    private final double fov;
    private final double fovRad;
    private final double aspectRatio;
    private final Matrix4 projectionMatrix;

    public Projector(int width, int height, double zoom) {
        this.width = width;
        this.height = height;
        near = 0.1;
        far = 1000;
        fov = 90;
        fovRad = 1.0 / Math.tan(fov * 0.5 / 180 * Math.PI);
        aspectRatio = (double) height / width;
        projectionMatrix = new Matrix4(new double[][]{
                {aspectRatio * fovRad, 0, 0, 0},
                {0, fovRad, 0, 0},
                {0, 0, far / (far - near), 1.0},
                {0, 0, (-far * near) / (far - near), 0},
        });
    }

    public Vector2D project(Vector3D pos) {
        Vector3D projected = projectionMatrix.multiply(pos);
        // Reposition so that 0,0 is in the center of the window
        return new Vector2D(projected.x(), projected.y()).add(new Vector2D(1, 1)).scale((double) width / 2);
    }
}
