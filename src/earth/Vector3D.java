package earth;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public record Vector3D(double x, double y, double z) {

    public Vector3D add(Vector3D b) {
        return new Vector3D(x + b.x, y + b.y, z + b.z);
    }

    public Vector3D subtract(Vector3D b) {
        return new Vector3D(x - b.x, y - b.y, z - b.z);
    }

//    /**
//     * Returns a new vector representing the current vector rotated anti-clockwise by angle radians
//     * @param angle the angle to rotate in radians
//     * @return the new rotated vector
//     */
//    public Vector3D rotate(double angle) {
//        return new Vector3D(Math.cos(angle) * x - Math.sin(angle) * y, Math.sin(angle) * x + Math.cos(angle) * y);
//    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double magnitude2() {
        return x * x + y * y + z * z;
    }

    public Vector3D scale(double scale) {
        return new Vector3D(x * scale, y * scale, z * scale);
    }

    public Vector3D scaleTo(double scale) {
        double magnitude = magnitude();
        return new Vector3D(scale * x / magnitude, scale * y / magnitude, scale * z / magnitude);
    }

    public Vector3D unit() {
        double magnitude = magnitude();
        return new Vector3D(x / magnitude, y / magnitude, z / magnitude);
    }

    public double dot(Vector3D b) {
        return x * b.x + y * b.y + z * b.z;
    }

    public Vector3D cross(Vector3D b) {
        return new Vector3D(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x);
    }

}
