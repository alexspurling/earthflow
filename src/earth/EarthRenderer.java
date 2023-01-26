package earth;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class EarthRenderer implements CanvasRenderer, MouseMotionListener {


    public static final int WIDTH = 1024;
    public static final int HEIGHT = 1024;

    private static final double ZOOM_LEVEL = 100000;

    private final Projector projector = new Projector(WIDTH, HEIGHT, ZOOM_LEVEL);

    private final BufferedImage img;
    private final double distance;
    private final double scaleFactor;

    private int frameCount = 0;
    private long lastFpsTime = System.currentTimeMillis();
    private int mouseX;
    private int mouseY;
    private Cube cube;

    private long lastFrameTime;

    public EarthRenderer() {
        try {
            img = ImageIO.read(new File("images/epic_1b_20230119000830.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        distance = Math.sqrt(Math.pow(513256.302301, 2) + Math.pow(-1132637.821089, 2) + Math.pow(-676524.885803, 2));
        scaleFactor = 1.05;
        cube = new Cube(new Vector3D(0, 0, 5), 0.0005, 0.001);
    }

    @Override
    public void render(Graphics g) {
        long curFrameTime = System.nanoTime();
        double dt = (double)(curFrameTime - lastFrameTime) / 1e6; // Convert nanoseconds to milliseconds
        lastFrameTime = curFrameTime;
        render(g, dt, false);
    }

    @Override
    public void render(Graphics g, double dt, boolean recordMode) {

        g.setColor(new Color(123, 234, 12));
        g.drawImage(img, 0, 0, WIDTH, HEIGHT, null);

        var mousePixel = new Color(img.getRGB(mouseX * 2, mouseY * 2));

        g.drawString("Mouse: x: " + mouseX + ", y: " + mouseY, 800, 50);
        g.drawString("Pixel: (" + mousePixel.getRed() + ", " + mousePixel.getGreen() + ", " + mousePixel.getBlue() + ")", 800, 70);

        Vector2D northPos = getNorthPole();
        Vector2D southPos = getSouthPole();

        int radius = 5;
//        g.drawOval((int) northPos.x(), (int) northPos.y(), radius, radius);
//        g.drawOval((int) southPos.x(), (int) southPos.y(), radius, radius);

        cube.update(dt);

        for (Triangle tri : cube.getTriangles()) {
            drawTriangle(g, tri);
        }

        frameCount++;
        long time = System.currentTimeMillis();
        if (time - lastFpsTime > 1000) {
            System.out.println("FPS: " + frameCount);
            frameCount = 0;
            lastFpsTime = time;
        }
    }

    private void drawTriangle(Graphics g, Triangle tri) {
        // Check if triangle is facing towards or away from the camera
        Vector3D normal = tri.normal();
        double normalDotOrigin = normal.dot(tri.a());
        if (normalDotOrigin > 0) return;

        Vector2D projectedA = projector.project(tri.a());
        Vector2D projectedB = projector.project(tri.b());
        Vector2D projectedC = projector.project(tri.c());
        g.drawLine((int) projectedA.x(), (int) projectedA.y(), (int) projectedB.x(), (int) projectedB.y());
        g.drawLine((int) projectedB.x(), (int) projectedB.y(), (int) projectedC.x(), (int) projectedC.y());
        g.drawLine((int) projectedC.x(), (int) projectedC.y(), (int) projectedA.x(), (int) projectedA.y());
    }

    // TODO also add in earth's rotation around the sun which affects the apparent tilt
    private Vector2D getSouthPole() {
        double tiltAngle = 23.4;
        double earthPolarRadius = -6356.752;
        double polePosition = Math.cos(tiltAngle * Math.PI / 180) * earthPolarRadius;
        return projector.project(new Vector3D(0, polePosition, distance * scaleFactor));
    }

    // TODO also add in earth's rotation around the sun which affects the apparent tilt
    private Vector2D getNorthPole() {
        double tiltAngle = 23.4;
        double earthPolarRadius = 6356.752;
        double polePosition = Math.cos(tiltAngle * Math.PI / 180) * earthPolarRadius;
        return projector.project(new Vector3D(0, polePosition, distance * scaleFactor));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }
}
