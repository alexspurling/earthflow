package earth;

import java.awt.image.BufferedImage;
import java.time.OffsetDateTime;

public class EarthTexture {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;

    private final BufferedImage earthTexture;

    public EarthTexture(Sphere sphere, BufferedImage earthImage, OffsetDateTime dateTime) {
        earthTexture = renderEarthTexture(sphere, earthImage);
    }

    private BufferedImage renderEarthTexture(Sphere sphere, BufferedImage earthImage) {

        Vector3D camera = new Vector3D(0, 0, 0);
        Vector3D lightDirection = new Vector3D(0, 0, 1);

        // Trace rays to generate earth texture
//        IntStream.range(0, WIDTH).parallel().forEach((x) -> {

        BufferedImage earthTexture = new BufferedImage(WIDTH * 4, HEIGHT * 2, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {

                double x3d = (double) x / (WIDTH / 2.0) - 1;
                double y3d = (double) (HEIGHT - y) / (HEIGHT / 2.0) - 1;
                Vector3D ray = new Vector3D(x3d, y3d, 1);

                Intersection intersection = sphere.getIntersection(ray, camera);

                if (intersection == null) continue;

                double normalDotCamera = intersection.normal().dot(intersection.point().subtract(camera));
                if (normalDotCamera > 0) continue;

                double normalDotLight = intersection.normal().dot(lightDirection);
                if (normalDotLight >= -1 && normalDotLight <= 0) {
                    setMappedTextureColour(x, y, intersection.point().subtract(sphere.position), earthImage, earthTexture);
                }
            }
        }

        return earthTexture;
    }


    public void setMappedTextureColour(int x, int y, Vector3D point, BufferedImage earthImage, BufferedImage earthTexture) {
        double u = 0.5 + Math.atan2(point.z(), point.x()) / (Math.PI * 2);
        double v = 0.5 + Math.asin(point.y()) / Math.PI;

        int worldColour = earthImage.getRGB(x * 2, y * 2);
        earthTexture.setRGB((int) (earthTexture.getWidth() * u), (int) (earthTexture.getHeight() * v), worldColour);
    }

    public BufferedImage getEarthTexture() {
        return earthTexture;
    }
}
