package earth;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class Sphere {

    private static final double MAX_TILT = Math.toRadians(23.4);
    private Vector3D position;
    private double radius;
    private BufferedImage earthImage;

    private Quaternion rotation;
    private OffsetDateTime dateTime;

    public Sphere(Vector3D position, double radius, BufferedImage earthImage) {
        this.position = position;
        this.radius = radius;
        this.earthImage = earthImage;
        this.rotation = getRotation(0);
        //noinspection DataFlowIssue
        this.dateTime = OffsetDateTime.of(2023, 1, 19, 0, 3, 42, 0, ZoneOffset.UTC);
    }

    private Quaternion getRotation(double daysSinceWinterSolstice) {
        double yearInRadians = daysSinceWinterSolstice / 365.25 * (Math.PI * 2);
        double xAxisTilt = MAX_TILT * Math.cos(yearInRadians);
        double zAxisTilt = 0; // MAX_TILT * Math.sin(yearInRadians); // no need to rotate around the z axis as the DSCOVR satellite compensates for this rotation in its images
        return new Quaternion(xAxisTilt, new Vector3D(-1, 0, 0))
                .multiply(new Quaternion(zAxisTilt, new Vector3D(0, 0, 1)));
    }

    public void update(OffsetDateTime dateTime) {
//        rotation = getRotation(days);
        this.dateTime = dateTime;
        rotation = getRotation(29).multiply(new Quaternion(0.1 * days / 365.25 * (Math.PI * 2), new Vector3D(0, 1, 0)));
    }

    public Intersection getIntersection(Vector3D ray, Vector3D rayOrigin) {

//        Vector3D transformedPosition = viewMatrix.multiply(position);
        Vector3D transformedPosition = position;

        Vector3D rayToCentre = transformedPosition.subtract(rayOrigin);
        Vector3D unitRay = ray.unit();
        double rayComponent = rayToCentre.dot(unitRay);
        double rayToCentreLength = rayToCentre.magnitude();
        double distanceFromCentre = Math.sqrt((rayToCentreLength * rayToCentreLength) - (rayComponent * rayComponent));

        if (distanceFromCentre > radius) {
            return null;
        }

        double intersectionDistance = Math.sqrt(radius * radius - distanceFromCentre * distanceFromCentre);

        Vector3D intersectionPoint = rayOrigin.add(unitRay.scaleTo(rayComponent - intersectionDistance));
        Vector3D normal = intersectionPoint.subtract(transformedPosition).scale(1 / radius);

        return new Intersection(intersectionPoint, normal);
    }

    public Color getTextureColour(Vector3D point, BufferedImage earthTexture) {
        Vector3D d = point.subtract(position).unit();
        d = rotation.rotatePoint(d);
        double u = 0.5 + Math.atan2(d.z(), d.x()) / (Math.PI * 2);
        double v = 0.5 + Math.asin(d.y()) / Math.PI;

        Color chequeredColour;

        if ((int)(u * 10) % 2 == 0) {
            if ((int)(v * 10) % 2 == 0) {
                chequeredColour = new Color(0, 0, 0);
            } else {
                chequeredColour = new Color(255, 255, 255);
            }
        } else {
            if ((int) (v * 10) % 2 == 0) {
                chequeredColour = new Color(255, 255, 255);
            } else {
                chequeredColour = new Color(0, 0, 0);
            }
        }

        // Based on the current date / time, find the nearest two earth textures and the distance between them
        // Load all the earth images and create a map by date.
        // Load the required pixel as a combination of two earth textures.
        int uint = (int) (u * earthTexture.getWidth());
        int vint = (int) (v * earthTexture.getHeight());
        Color earthTextureColour = new Color(earthTexture.getRGB(uint, vint));
        int k = 3;
        while (earthTextureColour.equals(Color.BLACK) && k <= 9) {
            // try to find a nearby pixel to interpolate with
            earthTextureColour = averagePixels(earthTexture, uint, vint, k);
            k += 2;
        }
        return lerpColor(chequeredColour, earthTextureColour, 0.9f);
    }

    private Color averagePixels(BufferedImage earthTexture, int x, int y, int kernelSize) {
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
            return Color.BLACK;
        }
        int averageRed = sumRed / count;
        int averageGreen = sumGreen / count;
        int averageBlue = sumBlue / count;
        return new Color(averageRed, averageGreen, averageBlue);
    }

    private static Color lerpColor(Color colorA, Color colorB, float blend) {
        int red = (int) (colorA.getRed() * (1 - blend) + colorB.getRed() * blend);
        int green = (int) (colorA.getGreen() * (1 - blend) + colorB.getGreen() * blend);
        int blue = (int) (colorA.getBlue() * (1 - blend) + colorB.getBlue() * blend);
        return new Color(red, green, blue);
    }

    public void setMappedTextureColour(int x, int y, Vector3D point, BufferedImage earthTexture) {
        Vector3D d = point.subtract(position).unit();
        d = rotation.rotatePoint(d);
        double u = 0.5 + Math.atan2(d.z(), d.x()) / (Math.PI * 2);
        double v = 0.5 + Math.asin(d.y()) / Math.PI;

        int worldColour = earthImage.getRGB(x * 2, y * 2);
        Color worldColourColor = new Color(worldColour);
        if (worldColour == -16777216) {
            // earthTexture.setRGB((int) (earthTexture.getWidth() * u), (int) (earthTexture.getHeight() * v), new Color(0, 200, 0).getRGB());
        } else {
            earthTexture.setRGB((int) (earthTexture.getWidth() * u), (int) (earthTexture.getHeight() * v), worldColour);
        }
    }

}
