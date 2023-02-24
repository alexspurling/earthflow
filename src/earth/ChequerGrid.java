package earth;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ChequerGrid {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;

    private final BufferedImage texture;

    public ChequerGrid() {
        texture = renderEarthTexture();
    }

    private BufferedImage renderEarthTexture() {

        BufferedImage earthTexture = new BufferedImage(WIDTH * 4, HEIGHT * 2, BufferedImage.TYPE_INT_RGB);

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

    public BufferedImage getImage() {
        return texture;
    }
}
