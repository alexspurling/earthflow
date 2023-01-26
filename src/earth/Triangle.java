package earth;

public record Triangle(Vector3D a, Vector3D b, Vector3D c) {

    public Triangle rotateZ(double angle) {
        return new Triangle(a.rotateZ(angle), b.rotateZ(angle), c.rotateZ(angle));
    }

    public Triangle rotateX(double angle) {
        return new Triangle(a.rotateX(angle), b.rotateX(angle), c.rotateX(angle));
    }

    public Triangle add(Vector3D vec) {
        return new Triangle(a.add(vec), b.add(vec), c.add(vec));
    }

    public Vector3D normal() {
        Vector3D line1 = b.subtract(a);
        Vector3D line2 = c.subtract(a);
        return line1.cross(line2).unit();
    }
}
