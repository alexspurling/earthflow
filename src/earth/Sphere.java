package earth;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Sphere {

    private static final double MAX_TILT = Math.toRadians(23.4);
    private Vector3D position;
    private double radius;
    private BufferedImage texture;

    private Quaternion rotation;

    public Sphere(Vector3D position, double radius, BufferedImage texture) {
        this.position = position;
        this.radius = radius;
        this.texture = texture;
        this.rotation = getRotation(0);
    }

    private Quaternion getRotation(double daysSinceWinterSolstice) {
        double yearInRadians = daysSinceWinterSolstice / 365.25 * (Math.PI * 2);
        double xAxisTilt = MAX_TILT * Math.cos(yearInRadians);
        double zAxisTilt = 0; // MAX_TILT * Math.sin(yearInRadians); // no need to rotate around the z axis as the DSCOVR satellite compensates for this rotation in its images
        return new Quaternion(xAxisTilt, new Vector3D(-1, 0, 0))
                .multiply(new Quaternion(zAxisTilt, new Vector3D(0, 0, 1)));
    }

    public void update(double days) {
        rotation = getRotation(days);
    }

    public Intersection getIntersection(Vector3D ray, Vector3D rayOrigin, Matrix4 viewMatrix) {

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

    public Color getTextureColour(Vector3D point) {
        Vector3D d = point.subtract(position).unit();
        d = rotation.rotatePoint(d);
        double u = 0.5 + Math.atan2(d.z(), d.x()) / (Math.PI * 2);
        double v = 0.5 + Math.asin(d.y()) / Math.PI;
        if ((int)(u * 10) % 2 == 0) {
            if ((int)(v * 10) % 2 == 0) {
                return new Color(0, 0, 0);
            }
            return new Color(255, 255, 255);
        } else {
            if ((int) (v * 10) % 2 == 0) {
                return new Color(255, 255, 255);
            }
            return new Color(0, 0, 0);
        }
    }


}
