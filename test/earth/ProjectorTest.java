package earth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectorTest {

    @Test
    public void test3DTo2D() {
        Projector p = new Projector(1000, 1000);

        // Top left
        Vector2D topLeft = p.project(new Vector3D(-1, 1, 0));
        assertEquals(0, topLeft.x(), 1e-10);
        assertEquals(0, topLeft.y(), 1e-10);

        // Top right
        Vector2D topRight = p.project(new Vector3D(1, 1, 0));
        assertEquals(1000, topRight.x(), 1e-10);
        assertEquals(0, topRight.y(), 1e-10);

        // Bottom left
        Vector2D bottomLeft = p.project(new Vector3D(-1, -1, 0));
        assertEquals(0, bottomLeft.x(), 1e-10);
        assertEquals(1000, bottomLeft.y(), 1e-10);

        // Bottom right
        Vector2D bottomRight = p.project(new Vector3D(1, -1, 0));
        assertEquals(1000, bottomRight.x(), 1e-10);
        assertEquals(1000, bottomRight.y(), 1e-10);
    }

    @Test
    public void test2DTo3D() {
        Projector p = new Projector(1000, 1000);

        for (int x = 0; x <= 1000; x++) {
            for (int y = 0; y <= 1000; y++) {
                double x3d = (double) x / 500 - 1;
                double y3d = (double) (1000 - y) / 500 - 1;
                Vector2D point = p.project(new Vector3D(x3d, y3d, 1));

                System.out.println("X: " + x + ", y: " + y + ", x3d: " + x3d + ", y3d: " + y3d + ", px: " + point.x() + ", py: " + point.y());
                assertEquals(x, point.x(), 1e-10);
                assertEquals(y, point.y(), 1e-10);
            }
        }
    }

}
