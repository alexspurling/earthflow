package earth;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.OffsetDateTime;

public class EarthTexture implements Texture {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;

    private static final double FOV = 0.62;
    public static final double RAY_Z = Math.tan(Math.toRadians(90 - (FOV / 2)));

    private final Sphere sphere;
    private final EarthImage image;
    private final BufferedImage earthTexture;

    public EarthTexture(Sphere sphere, EarthImage image) {
        this.sphere = sphere;
        this.image = image;
        earthTexture = renderEarthTexture(image.image());
    }

    private BufferedImage renderEarthTexture(BufferedImage earthImage) {

        Vector3D camera = new Vector3D(0, 0, 0);
        Vector3D lightDirection = new Vector3D(0, 0, 1);

        // Trace rays to generate earth texture
//        IntStream.range(0, WIDTH).parallel().forEach((x) -> {

        long startTime = System.currentTimeMillis();
        BufferedImage earthTexture = new BufferedImage(WIDTH * 4, HEIGHT * 2, BufferedImage.TYPE_INT_RGB);
        System.out.println("Created buffered image in " + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();

        int width = WIDTH * 2;
        int height = HEIGHT * 2;

        double lastUX = -1;
        double lastVY = -1;

        double yDelta = 1;

        for (int x = 0; x < width; x++) {
            double y = 0;
            while (y < height) {

                double x3d = (double) x / (width / 2.0) - 1;
                double y3d = (double) (height - y) / (height / 2.0) - 1;
                Vector3D ray = new Vector3D(x3d, y3d, RAY_Z);

                Intersection intersection = sphere.getIntersection(ray, camera);

                if (intersection != null) {

                    double normalDotCamera = intersection.normal().dot(intersection.point().subtract(camera));
                    if (normalDotCamera > 0) continue;

                    double normalDotLight = intersection.normal().dot(lightDirection);
                    if (normalDotLight >= -1 && normalDotLight <= 0) {

                        Vector3D d = intersection.point().subtract(sphere.position).unit();
                        d = sphere.getRotation(image.metadata().date()).rotatePoint(d);
                        double u = 0.5 + Math.atan2(d.z(), d.x()) / (Math.PI * 2);
                        double v = 0.5 + Math.asin(d.y()) / Math.PI;

                        int worldColour = earthImage.getRGB(x, (int) y);
                        double ux = earthTexture.getWidth() * u;
                        double vy = earthTexture.getHeight() * v;

                        earthTexture.setRGB((int) ux, (int) vy, worldColour);

                        if (lastUX != -1) {
                            double uvDifference = Math.sqrt((ux - lastUX) * (ux - lastUX) + (vy - lastVY) * (vy - lastVY));
                            yDelta /= uvDifference;

//                            if (yDelta < 0.0001) {
//                                System.out.println("x: " + x + ", y: " + y + ", yDelta: " + yDelta + ", uvDifference: " + uvDifference);
//                            }
                        }

                        lastUX = ux;
                        lastVY = vy;
                    }
                }
                y += yDelta;
            }
        }
        System.out.println("Ray traced texture " + (System.currentTimeMillis() - startTime));

        int black = Color.BLACK.getRGB();

        int numPixels = 0;

        startTime = System.currentTimeMillis();
//        for (int x = 0; x < earthTexture.getWidth(); x++) {
//            for (int y = 0; y < earthTexture.getHeight(); y++) {
//                int k = 1;
//                while (earthTexture.getRGB(x, y) == black && k <= 3) {
//                    // try to find a nearby pixel to interpolate with
//                    earthTexture.setRGB(x, y, averagePixels(earthTexture, x, y, k));
//                    k += 2;
//                    numPixels += 1;
//                }
//            }
//        }
        System.out.println("Filled in " + numPixels + " gaps in " + (System.currentTimeMillis() - startTime));

        System.out.println("Done");

        return earthTexture;
    }


    private int averagePixels(BufferedImage earthTexture, int x, int y, int kernelSize) {
        int kernelRadius = kernelSize / 2;
        int sumRed = 0;
        int sumGreen = 0;
        int sumBlue = 0;
        int count = 0;
        for (int i = -kernelRadius; i <= kernelRadius; i++) {
            for (int j = -kernelRadius; j <= kernelRadius; j++) {
                int xIndex = x + i;
                int yIndex = y + j;
                if (xIndex >= 0 && xIndex < earthTexture.getWidth() && yIndex >= 0 && yIndex < earthTexture.getHeight()) {
                    Color pixelColor = new Color(earthTexture.getRGB(xIndex, yIndex));
                    if (!pixelColor.equals(Color.BLACK)) {
                        sumRed += pixelColor.getRed();
                        sumGreen += pixelColor.getGreen();
                        sumBlue += pixelColor.getBlue();
                        count++;
                    }
                }
            }
        }
        if (count == 0) {
            return Color.BLACK.getRGB();
        }
        int averageRed = sumRed / count;
        int averageGreen = sumGreen / count;
        int averageBlue = sumBlue / count;
        return new Color(averageRed, averageGreen, averageBlue).getRGB();
    }

    @Override
    public BufferedImage getTexture() {
        return earthTexture;
    }

    @Override
    public OffsetDateTime getDate() {
        return image.metadata().date();
    }

    public EarthImage getEarthImage() {
        return image;
    }
}
