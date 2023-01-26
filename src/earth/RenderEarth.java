package earth;

public class RenderEarth {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;

    public static void main(String[] args) {

        EarthRenderer earthRenderer = new EarthRenderer();

        GraphicsCanvas main = new GraphicsCanvas(earthRenderer, WIDTH, HEIGHT);

        main.start();

        main.addMouseMotionListener(earthRenderer);
    }
}
