package earth;

public record Matrix4(double[][] m) {

    public Vector3D multiply(Vector3D i) {
        double x = i.x() * m[0][0] + i.y() * m[1][0] + i.z() * m[2][0] + m[3][0];
        double y = i.x() * m[0][1] + i.y() * m[1][1] + i.z() * m[2][1] + m[3][1];
        double z = i.x() * m[0][2] + i.y() * m[1][2] + i.z() * m[2][2] + m[3][2];
        double w = i.x() * m[0][3] + i.y() * m[1][3] + i.z() * m[2][3] + m[3][3];

        if (w != 0) {
            return new Vector3D(x / w, y / w, z / w);
        }
        return new Vector3D(x, y, z);
    }
}
