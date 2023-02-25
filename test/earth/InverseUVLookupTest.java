package earth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InverseUVLookupTest {

    @Test
    public void testUVLookup() {
        Vector3D d = new Vector3D(-0.7342336515913488, -0.6355037502617586, -0.23882195936319195);
        double u = 0.5 + Math.atan2(d.z(), d.x()) / (Math.PI * 2);
        double v = 0.5 + Math.asin(d.y()) / Math.PI;

        System.out.println(u);
        System.out.println(v);
    }

    @Test
    public void testInvUVLookup() {
        for (int i = 0; i < 100000; i++) {

            double x1 = Math.random() * 2 - 1;
            double y1 = Math.random() * 2 - 1;
            double z1 = Math.random() * 2 - 1;
            Vector3D d = new Vector3D(x1, y1, z1).unit();

            double u = 0.5 + Math.atan2(d.z(), d.x()) / (Math.PI * 2);
            double v = 0.5 + Math.asin(d.y()) / Math.PI;

            double PI = Math.PI;

            double dx = Math.cos(PI * (0.5 - v)) * Math.cos(2 * PI * (u - 0.5));
            double dy = Math.sin(PI * (v - 0.5));
            double dz = Math.cos(PI * (0.5 - v)) * Math.sin(2 * PI * (u - 0.5));

            assertEquals(d.x(), dx, 1e-8);
            assertEquals(d.y(), dy, 1e-8);
            assertEquals(d.z(), dz, 1e-8);
        }
    }
}
