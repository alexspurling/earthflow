package earth;

import org.junit.jupiter.api.Test;

public class QuaternionTest {

    @Test
    public void testAddAndSubtract() {
        Quaternion rotation = new Quaternion(0.983637530728208, -0.17997791527908777, 0.007940239981918132, 0.001452839886765318);

        Vector3D i3 = new Vector3D(-0.9991295642048261, 0.0398713675856386, -0.012263277650177007);
        Vector3D d = rotation.rotatePoint(i3);

        System.out.println(d);

        Vector3D invD = rotation.inverse().rotatePoint(d);

        System.out.println(invD);
        System.out.println(i3);
    }
}
