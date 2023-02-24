package earth;

import java.awt.image.BufferedImage;
import java.time.OffsetDateTime;

public class ChequerTexture implements Texture {

    private final BufferedImage texture;
    private final OffsetDateTime dateTime;

    public ChequerTexture(BufferedImage texture, OffsetDateTime dateTime) {
        this.texture = texture;
        this.dateTime = dateTime;
    }

    @Override
    public BufferedImage getTexture() {
        return texture;
    }

    @Override
    public OffsetDateTime getDate() {
        return dateTime;
    }
}
