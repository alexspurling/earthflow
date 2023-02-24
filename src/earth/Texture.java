package earth;

import java.awt.image.BufferedImage;
import java.time.OffsetDateTime;

public interface Texture {

    BufferedImage getTexture();

    OffsetDateTime getDate();
}
