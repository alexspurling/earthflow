package earth;

import java.awt.image.BufferedImage;

/**
 * Represents a photo of the earth taken by the DSCOVR satellite at a given point in time
 */
public record EarthImage(ImageMetadata metadata, BufferedImage image) {
}
