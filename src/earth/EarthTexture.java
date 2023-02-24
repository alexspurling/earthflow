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

        // Trace rays to generate earth texture
        BufferedImage earthTexture = new BufferedImage(WIDTH * 4, HEIGHT * 2, BufferedImage.TYPE_INT_RGB);

        long startTime = System.currentTimeMillis();

        int width = WIDTH * 2;
        int height = HEIGHT * 2;

        Quaternion sphereRotation = sphere.getRotation(image.metadata().date());
        Quaternion sphereRotationInv = sphereRotation.inverse();

        int tWidth = width * 2;
        int tHeight = height;

        try {
            // For each pixel in the texture:
            // 1. Find the point in on a 3d sphere that it maps to
            // 2. Project a ray backwards to find the 2d x,y "screen" coordindate
            // that would intersect this point
            // 3. Grab the pixel colour of the earth image at these x,y coords
            for (int ux = 0; ux < WIDTH * 4; ux++) {
                for (int vy = 0; vy < WIDTH * 2; vy++) {

                    if (ux == 205 && vy == 575) {
                        System.out.println("Stop");
                    }
                    double u = (double) ux / tWidth;
                    double v = (double) vy / tHeight;

                    // Invert the UV mapping. Taken from:
                    // https://math.stackexchange.com/questions/1395679/how-would-i-find-a-point-on-a-sphere-with-a-uv-coordinate
                    double dx = Math.cos(Math.PI * (0.5 - v)) * Math.cos(2 * Math.PI * (u - 0.5));
                    double dy = Math.sin(Math.PI * (v - 0.5));
                    double dz = Math.cos(Math.PI * (0.5 - v)) * Math.sin(2 * Math.PI * (u - 0.5));

                    Vector3D d = new Vector3D(dx, dy, dz);
                    Vector3D i3 = sphereRotationInv.rotatePoint(d);
                    Vector3D i2 = i3.scale(sphere.radius);

                    if (i2.z() > 0) {
                        continue;
                    }
                    Vector3D intersection = i2.add(sphere.position);

                    double x3d = intersection.x() * RAY_Z / intersection.z();
                    double y3d = intersection.y() * RAY_Z / intersection.z();

                    double x = (x3d + 1) * (width / 2.0);
                    double y = height - (y3d + 1) * (height / 2.0);

                    x = Math.max(Math.min(x, width - 1), 0);
                    y = Math.max(Math.min(y, width - 1), 0);

                    int colour = earthImage.getRGB((int) x, (int) y);
                    earthTexture.setRGB(ux, vy, colour);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("Oh dear");
        }
        System.out.println("Ray traced texture " + (System.currentTimeMillis() - startTime));

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
