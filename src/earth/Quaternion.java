package earth;

public record Quaternion(double w, double x, double y, double z) {

    public Quaternion(double theta, Vector3D axis) {
        this(Math.cos(theta / 2), axis.x() * Math.sin(theta / 2), axis.y() * Math.sin(theta / 2), axis.z() * Math.sin(theta / 2));
    }

    public double getAngle() {
        return 2 * Math.acos(w);
    }

    public Vector3D getAxis() {
        double theta = getAngle();
        double sinTheta2 = Math.sin(theta / 2);
        if (theta != 0) {
            return new Vector3D(x / sinTheta2, y / sinTheta2, z / sinTheta2);
        }
        return new Vector3D(1, 0, 0);
    }

    public Quaternion multiply(Quaternion b) {
        return new Quaternion(
                w * b.w - x * b.x - y * b.y - z * b.z,
                w * b.x + x * b.w - y * b.z + z * b.y,
                w * b.y + x * b.z + y * b.w - z * b.x,
                w * b.z - x * b.y + y * b.x + z * b.w
                );
    }

    public Quaternion inverse() {
        return new Quaternion(w, -x, -y, -z);
    }

    public Vector3D rotatePoint(Vector3D p) {
        Quaternion pointQuat = new Quaternion(0, p.x(), p.y(), p.z());
        Quaternion rotated = this.inverse().multiply(pointQuat).multiply(this);
        return new Vector3D(rotated.x, rotated.y, rotated.z);
    }

}
