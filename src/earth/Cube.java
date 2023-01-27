package earth;

import java.util.List;
import java.util.stream.Collectors;

public class Cube implements Mesh {

    private final List<Triangle> tris;
    private final Vector3D pos;
    private final double spinX;
    private final double spinY;
    private final double spinZ;

    private double rotateX;
    private double rotateY;
    private double rotateZ;

    public Cube(Vector3D pos, double spinX, double spinY, double spinZ) {
        this.pos = pos;
        this.spinX = spinX;
        this.spinY = spinY;
        this.spinZ = spinZ;
        this.tris = List.of(
                // South
                new Triangle(new Vector3D(-1, -1, -1), new Vector3D(-1, 1, -1), new Vector3D(1, 1, -1)),
                new Triangle(new Vector3D(-1, -1, -1), new Vector3D(1, 1, -1), new Vector3D(1, -1, -1)),
                // East
                new Triangle(new Vector3D(1, -1, -1), new Vector3D(1, 1, -1), new Vector3D(1, 1, 1)),
                new Triangle(new Vector3D(1, -1, -1), new Vector3D(1, 1, 1), new Vector3D(1, -1, 1)),
                // North
                new Triangle(new Vector3D(1, -1, 1), new Vector3D(1, 1, 1), new Vector3D(-1, 1, 1)),
                new Triangle(new Vector3D(1, -1, 1), new Vector3D(-1, 1, 1), new Vector3D(-1, -1, 1)),
                // West
                new Triangle(new Vector3D(-1, -1, 1), new Vector3D(-1, 1, 1), new Vector3D(-1, 1, -1)),
                new Triangle(new Vector3D(-1, -1, 1), new Vector3D(-1, 1, -1), new Vector3D(-1, -1, -1)),
                // Top
                new Triangle(new Vector3D(-1, 1, -1), new Vector3D(-1, 1, 1), new Vector3D(1, 1, 1)),
                new Triangle(new Vector3D(-1, 1, -1), new Vector3D(1, 1, 1), new Vector3D(1, 1, -1)),
                // Bottom
                new Triangle(new Vector3D(1, -1, 1), new Vector3D(-1, -1, 1), new Vector3D(-1, -1, -1)),
                new Triangle(new Vector3D(1, -1, 1), new Vector3D(-1, -1, -1), new Vector3D(1, -1, -1))
        );
    }

    @Override
    public void update(double dt) {
        this.rotateX += spinX * dt;
        this.rotateY += spinY * dt;
        this.rotateZ += spinZ * dt;
    }

    public Matrix4 getTransform() {
        // Return the transformation matrix that represents the translations and rotations needed for the cube
        Matrix4 transform = Matrix4.identity();
        transform = transform.rotateX(rotateX);
        transform = transform.rotateY(rotateY);
        transform = transform.rotateZ(rotateZ);
        transform = transform.translate(pos);
        return transform;
    }

    @Override
    public List<Triangle> getTriangles() {
        return tris;
    }
}
