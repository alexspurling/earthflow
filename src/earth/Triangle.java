package earth;

public record Triangle(Vector3D a, Vector3D b, Vector3D c) {

    public Vector3D normal() {
        Vector3D line1 = b.subtract(a);
        Vector3D line2 = c.subtract(a);
        return line1.cross(line2).unit();
    }

    public Triangle transform(Matrix4 transform) {
        return new Triangle(transform.multiply(a), transform.multiply(b), transform.multiply(c));
    }
}
