package earth;

import java.util.Arrays;

public record Matrix4(double[][] m) {

    public static Matrix4 identity() {
        return new Matrix4(new double[][] {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1},
        });
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                b.append(m[i][j]);
                b.append(", ");
            }
            b.append(m[i][3]);
            b.append("\n");
        }
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Matrix4 matrix4 = (Matrix4) o;
        return Arrays.deepEquals(m, matrix4.m);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(m);
    }

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

    public Matrix4 multiply(Matrix4 mat) {
        double[][] newMat = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += m[i][k] * mat.m[k][j];
                }
                newMat[i][j] = sum;
            }
        }
        return new Matrix4(newMat);
    }

    public Matrix4 rotateX(double angle) {
        return this.multiply(new Matrix4(new double[][] {
                {1, 0, 0, 0},
                {0, Math.cos(angle), Math.sin(angle), 0},
                {0, -Math.sin(angle), Math.cos(angle), 0},
                {0, 0, 0, 1}
        }));
    }

    public Matrix4 rotateY(double angle) {
        return this.multiply(new Matrix4(new double[][] {
                {Math.cos(angle), 0, Math.sin(angle), 0},
                {0, 1, 0, 0},
                {-Math.sin(angle), 0, Math.cos(angle), 0},
                {0, 0, 0, 1}
        }));
    }

    public Matrix4 rotateZ(double angle) {
        return this.multiply(new Matrix4(new double[][] {
                {Math.cos(angle), Math.sin(angle), 0, 0},
                {-Math.sin(angle), Math.cos(angle), 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        }));
    }

    public Matrix4 translate(Vector3D pos) {
        return this.multiply(new Matrix4(new double[][] {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {pos.x(), pos.y(), pos.z(), 1},
        }));
    }
}
