package earth;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

@SuppressWarnings("DataFlowIssue")
public class Sphere {

    private static final double MAX_TILT = Math.toRadians(23.4);
    private static final Temporal WINTER_2022 = OffsetDateTime.of(2022, 12, 21, 12, 0, 0, 0, ZoneOffset.UTC);
    public Vector3D position;
    private double radius;
    private static final long SECONDS_IN_DAY = 86400;

    private Quaternion rotation;
    private OffsetDateTime dateTime;

    public Sphere(Vector3D position, double radius) {
        this.position = position;
        this.radius = radius;
        this.dateTime = OffsetDateTime.of(2023, 1, 19, 0, 3, 42, 0, ZoneOffset.UTC);
        update(this.dateTime);
    }

    private Quaternion getSeasonalTilt(OffsetDateTime dateTime) {
        double daysSinceWinterSolstice = ChronoUnit.DAYS.between(dateTime, WINTER_2022);
        double yearInRadians = daysSinceWinterSolstice / 365.25 * (Math.PI * 2);
        double xAxisTilt = MAX_TILT * Math.cos(yearInRadians);
        double zAxisTilt = 0; // MAX_TILT * Math.sin(yearInRadians); // no need to rotate around the z axis as the DSCOVR satellite compensates for this rotation in its images
        return new Quaternion(xAxisTilt, new Vector3D(-1, 0, 0))
                .multiply(new Quaternion(zAxisTilt, new Vector3D(0, 0, 1)));
    }

    private Quaternion getDailyRotation(OffsetDateTime dateTime) {
        long seconds = ChronoUnit.SECONDS.between(dateTime.truncatedTo(ChronoUnit.DAYS), dateTime);
        double theta = ((double) seconds / SECONDS_IN_DAY) * Math.PI * 2;
        return new Quaternion(theta, new Vector3D(0, 1, 0));
    }

    public void update(OffsetDateTime dateTime) {
//        rotation = getRotation(days);
        this.dateTime = dateTime;
        rotation = getRotation(dateTime);
    }

    public Quaternion getRotation(OffsetDateTime dateTime) {
        return getSeasonalTilt(dateTime).multiply(getDailyRotation(dateTime));
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

    public Color getTextureColour(Vector3D point, BufferedImage earthTexture1, BufferedImage earthTexture2, double blend) {
        Vector3D d = point.subtract(position).unit();
        d = rotation.rotatePoint(d);
        double u = 0.5 + Math.atan2(d.z(), d.x()) / (Math.PI * 2);
        double v = 0.5 + Math.asin(d.y()) / Math.PI;

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

        // Based on the current date / time, find the nearest two earth textures and the distance between them
        // Load all the earth images and create a map by date.
        // Load the required pixel as a combination of two earth textures.
        int uint = (int) (u * earthTexture1.getWidth());
        int vint = (int) (v * earthTexture1.getHeight());
        Color earthTexture1Colour = new Color(earthTexture1.getRGB(uint, vint));
        Color earthTexture2Colour = new Color(earthTexture2.getRGB(uint, vint));

        return lerpColor(earthTexture1Colour, earthTexture2Colour, blend);
    }

    private static Color lerpColor(Color colorA, Color colorB, double blend) {
        int red = (int) (colorA.getRed() * (1 - blend) + colorB.getRed() * blend);
        int green = (int) (colorA.getGreen() * (1 - blend) + colorB.getGreen() * blend);
        int blue = (int) (colorA.getBlue() * (1 - blend) + colorB.getBlue() * blend);
        return new Color(red, green, blue);
    }

}
