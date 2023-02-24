package earth;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ChequerGrid {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;

    private static final double FOV = 0.62;
    public static final double RAY_Z = Math.tan(Math.toRadians(90 - (FOV / 2)));

    private final Sphere sphere;
    private final BufferedImage texture;

    public ChequerGrid(Sphere sphere) {
        this.sphere = sphere;
        texture = renderEarthTexture();
    }

    private BufferedImage renderEarthTexture() {

        Vector3D camera = new Vector3D(0, 0, 0);
        Vector3D lightDirection = new Vector3D(0, 0, 1);

        // Trace rays to generate earth texture
//        IntStream.range(0, WIDTH).parallel().forEach((x) -> {

        long startTime = System.currentTimeMillis();
        BufferedImage earthTexture = new BufferedImage(WIDTH * 4, HEIGHT * 2, BufferedImage.TYPE_INT_RGB);
        System.out.println("Created buffered image in " + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
//        for (int x = 0; x < WIDTH; x++) {
//            for (int y = 0; y < HEIGHT; y++) {
//
//                double x3d = (double) x / (WIDTH / 2.0) - 1;
//                double y3d = (double) (HEIGHT - y) / (HEIGHT / 2.0) - 1;
//                Vector3D ray = new Vector3D(x3d, y3d, RAY_Z);
//
//                Intersection intersection = sphere.getIntersection(ray, camera);
//
//                if (intersection == null) continue;
//
//                double normalDotCamera = intersection.normal().dot(intersection.point().subtract(camera));
//                if (normalDotCamera > 0) continue;
//
//                double normalDotLight = intersection.normal().dot(lightDirection);
//                if (normalDotLight >= -1 && normalDotLight <= 0) {
//                    setMappedTextureColour(x, y, intersection.point(), earthTexture);
//                }
//            }
//        }
        System.out.println("Ray traced texture " + (System.currentTimeMillis() - startTime));

//        int black = Color.BLACK.getRGB();

//        int numPixels = 0;

//        startTime = System.currentTimeMillis();

        int xDivisions = 8;
        int yDivisions = 8;

        for (int x = 0; x < earthTexture.getWidth(); x++) {
            for (int y = 0; y < earthTexture.getHeight(); y++) {

                Color chequeredColour;
                if ((x * xDivisions / earthTexture.getWidth()) % 2 == 0) {
                    if ((y * yDivisions / earthTexture.getHeight()) % 2 == 0) {
                        chequeredColour = new Color(0, 0, 0);
                    } else {
                        chequeredColour = new Color(255, 255, 255);
                    }
                } else {
                    if ((y * yDivisions / earthTexture.getHeight()) % 2 == 0) {
                        chequeredColour = new Color(255, 255, 255);
                    } else {
                        chequeredColour = new Color(0, 0, 0);
                    }
                }
                earthTexture.setRGB(x, y, chequeredColour.getRGB());
            }
        }

        return earthTexture;
    }


//    public void setMappedTextureColour(int x, int y, Vector3D point, BufferedImage earthTexture) {
//        Vector3D d = point.subtract(sphere.position).unit();
////        d = sphere.getRotation(image.metadata().date()).rotatePoint(d);
//        double u = 0.5 + Math.atan2(d.z(), d.x()) / (Math.PI * 2);
//        double v = 0.5 + Math.asin(d.y()) / Math.PI;
//
//        Color chequeredColour;
//
//        if ((int)(u * 8) % 2 == 0) {
//            if ((int)(v * 8) % 2 == 0) {
//                chequeredColour = new Color(0, 0, 0);
//            } else {
//                chequeredColour = new Color(255, 255, 255);
//            }
//        } else {
//            if ((int) (v * 8) % 2 == 0) {
//                chequeredColour = new Color(255, 255, 255);
//            } else {
//                chequeredColour = new Color(0, 0, 0);
//            }
//        }
//
//        int worldColour = chequeredColour.getRGB();
//        earthTexture.setRGB((int) (earthTexture.getWidth() * u), (int) (earthTexture.getHeight() * v), worldColour);
//    }

    public BufferedImage getImage() {
        return texture;
    }
}
