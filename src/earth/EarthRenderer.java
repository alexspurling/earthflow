package earth;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class EarthRenderer implements CanvasRenderer, MouseListener, MouseMotionListener, KeyListener {


    public static final int WIDTH = 1024;
    public static final int HEIGHT = 1024;

    private static final double ZOOM_LEVEL = 100000;

    private final Projector projector = new Projector(WIDTH, HEIGHT);

    private final BufferedImage img;
    private final double distance;
    private final double scaleFactor;
    private final Sphere sphere;

    private int frameCount = 0;
    private long lastFpsTime = System.currentTimeMillis();
    private int mouseX;
    private int mouseY;

//    private Vector3D camera = new Vector3D(0, 0.5, -0.5);
    private Vector3D camera = new Vector3D(0, 0, 0);
    private Vector3D lookDir;
    private double yaw = 0;
    private double pitch = 0;

    private Vector3D lightDirection = new Vector3D(0, 0, 1);
    private Cube cube;

    private long lastFrameTime;
    private long startTime;

    private boolean mouseDown;
    private int mouseStartX;
    private int mouseStartY;
    private int mouseDeltaX;
    private int mouseDeltaY;
    private int mouseAccumX;
    private int mouseAccumY;
    private int mouseTotalX;
    private int mouseTotalY;
    private Set<Integer> pressedKeys = new HashSet<>();

    private int days = 0;

    public EarthRenderer() {
        BufferedImage texture;
        try {
            texture = ImageIO.read(new File("images/epic_1b_20230119000830.png"));
            img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        distance = Math.sqrt(Math.pow(513256.302301, 2) + Math.pow(-1132637.821089, 2) + Math.pow(-676524.885803, 2));
        scaleFactor = 1.05;
        cube = new Cube(new Vector3D(0, 0, 7), 0.0006, 0.002, 0.001);
        sphere = new Sphere(new Vector3D(0, 0, 7), 1.5, texture);
        startTime = System.currentTimeMillis();
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

//        dt = 0.2;

        g.setColor(new Color(123, 234, 12));
        g.drawImage(img, 0, 0, null);

        g.drawString("Mouse: x: " + mouseX + ", y: " + mouseY, 800, 50);
        if (mouseX > 0 && mouseX * 2 < img.getWidth() && mouseY > 0 && mouseY * 2 < img.getHeight()) {
            var mousePixel = new Color(img.getRGB(mouseX * 2, mouseY * 2));
            g.drawString("Pixel: (" + mousePixel.getRed() + ", " + mousePixel.getGreen() + ", " + mousePixel.getBlue() + ")", 800, 70);
        }
        g.drawString(String.format("Camera: x: %.2f, y: %.2f, z: %.2f", camera.x(), camera.y(), camera.z()), 800, 90);
        g.drawString(String.format("Days: " + days), 800, 110);

        Vector2D northPos = getNorthPole();
        Vector2D southPos = getSouthPole();

        int radius = 5;
//        g.drawOval((int) northPos.x(), (int) northPos.y(), radius, radius);
//        g.drawOval((int) southPos.x(), (int) southPos.y(), radius, radius);

        cube.update(dt);
        sphere.update(days++);

        Vector3D up = new Vector3D(0,1,0);
        Vector3D target = new Vector3D(0,0,1);

        if (pressedKeys.contains(KeyEvent.VK_W)) {
            camera = camera.add(new Vector3D(lookDir.x(), 0, lookDir.z()).scale(dt * 0.005));
        }
        if (pressedKeys.contains(KeyEvent.VK_S)) {
            camera = camera.add(new Vector3D(lookDir.x(), 0, lookDir.z()).scale(dt * -0.005));
        }
        if (pressedKeys.contains(KeyEvent.VK_A)) {
            camera = camera.add(lookDir.cross(up).scale(dt * 0.005));
        }
        if (pressedKeys.contains(KeyEvent.VK_D)) {
            camera = camera.add(lookDir.cross(up).scale(dt * -0.005));
        }
        if (pressedKeys.contains(KeyEvent.VK_SPACE)) {
            camera = camera.add(up.scale(dt * 0.005));
        }
        if (pressedKeys.contains(KeyEvent.VK_SHIFT)) {
            camera = camera.add(up.scale(dt * -0.005));
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        yaw = mouseTotalX * -0.002;
        pitch = mouseTotalY * 0.002;

        lookDir = Matrix4.identity().rotateX(pitch).rotateY(yaw).multiply(target);
        target = camera.add(lookDir);
        Matrix4 cameraMatrix = pointAt(camera, target, up);

        // Make view matrix from camera
        Matrix4 viewMatrix = cameraMatrix.invert();

        // Ray trace a sphere
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                // Reset the colour of each pixel
                img.setRGB(x, y, 0);

                double x3d = (double) x / (WIDTH / 2.0) - 1;
                double y3d = (double) (HEIGHT - y) / (HEIGHT / 2.0) - 1;
                Vector3D ray = new Vector3D(x3d, y3d, 1);

                Intersection intersection = sphere.getIntersection(ray, camera, viewMatrix);

                if (intersection == null) continue;

                double normalDotCamera = intersection.normal().dot(intersection.point().subtract(camera));
                if (normalDotCamera > 0) continue;

                double normalDotLight = intersection.normal().dot(lightDirection);
                if (normalDotLight >= -1 && normalDotLight <= 0) {
                    Color texColour = sphere.getTextureColour(intersection.point());
                    Color litColour = multiplyColour(texColour, -normalDotLight);
                    img.setRGB(x, y, litColour.getRGB());
//                    img.setRGB(x, y, new Color((int) (-normalDotLight * 255), 0, 0).getRGB());
                }
            }
        }

//        Matrix4 cubeWorldMatrix = cube.getTransform();
//        for (Triangle tri : cube.getTriangles()) {
//            drawTriangle(g, tri, cubeWorldMatrix, viewMatrix);
//        }
        drawAxes(g, viewMatrix);

        frameCount++;
        long time = System.currentTimeMillis();
        if (time - lastFpsTime > 1000) {
            System.out.println("FPS: " + frameCount + ", yaw: " + yaw + ", pitch: " + pitch);
            frameCount = 0;
            lastFpsTime = time;
        }
    }

    private Color multiplyColour(Color texColour, double multiplier) {
        multiplier = Math.min(1, Math.max(0, multiplier));
        return new Color(
                (int)(texColour.getRed() * multiplier),
                (int)(texColour.getGreen() * multiplier),
                (int)(texColour.getBlue() * multiplier));
    }

    Matrix4 pointAt(Vector3D pos, Vector3D target, Vector3D up) {
        Vector3D newForward = target.subtract(pos).unit();

        Vector3D a = newForward.scale(up.dot(newForward));
        Vector3D newUp = up.subtract(a).unit();

        Vector3D newRight = newUp.cross(newForward);
        return new Matrix4(new double[][] {
                {newRight.x(), newRight.y(), newRight.z(), 0},
                {newUp.x(), newUp.y(), newUp.z(), 0},
                {newForward.x(), newForward.y(), newForward.z(), 0},
                {pos.x(), pos.y(), pos.z(), 1},
        });
    }

    private void drawTriangle(Graphics g, Triangle tri, Matrix4 worldMatrix, Matrix4 viewMatrix) {

        Triangle transformed = tri.transform(worldMatrix);

        // Check if triangle is facing towards or away from the camera
        Vector3D normal = transformed.normal();

        double normalDotCamera = normal.dot(transformed.a().subtract(camera));
        if (normalDotCamera > 0) return;

        double normalDotLight = normal.dot(lightDirection);
        if (normalDotLight >= -1 && normalDotLight <= 0) {
            g.setColor(new Color(0, (int) (-normalDotLight * 255), 0));
        } else {
            g.setColor(new Color(0, 0, 0));
        }

        Triangle viewed = transformed.transform(viewMatrix);

        Vector2D projectedA = projector.project(viewed.a());
        Vector2D projectedB = projector.project(viewed.b());
        Vector2D projectedC = projector.project(viewed.c());
        g.drawLine((int) projectedA.x(), (int) projectedA.y(), (int) projectedB.x(), (int) projectedB.y());
        g.drawLine((int) projectedB.x(), (int) projectedB.y(), (int) projectedC.x(), (int) projectedC.y());
        g.drawLine((int) projectedC.x(), (int) projectedC.y(), (int) projectedA.x(), (int) projectedA.y());
        g.fillPolygon(new Polygon(new int[] {(int) projectedA.x(), (int) projectedB.x(), (int) projectedC.x()},
                new int[] {(int) projectedA.y(), (int) projectedB.y(), (int) projectedC.y()}, 3));
    }

    private void drawAxes(Graphics g, Matrix4 viewMatrix) {

        g.setColor(new Color(200, 200, 0));

        Vector3D origin = new Vector3D(0, 0, 0);
        Vector3D axisX = new Vector3D(1, 0, 0);
        Vector3D axisY = new Vector3D(0, 1, 0);
        Vector3D axisZ = new Vector3D(0, 0, 1);

        drawVector(g, viewMatrix, origin, axisX);
        drawVector(g, viewMatrix, origin, axisY);
        drawVector(g, viewMatrix, origin, axisZ);

        // X axis arrow
        drawVector(g, viewMatrix, axisX, new Vector3D(0.9, 0, -0.05));
        drawVector(g, viewMatrix, axisX, new Vector3D(0.9, 0, 0.05));

        // Y axis arrow
        drawVector(g, viewMatrix, axisY, new Vector3D(-0.05, 0.9, 0));
        drawVector(g, viewMatrix, axisY, new Vector3D(0.05, 0.9, 0));

        // Z axis arrow
        drawVector(g, viewMatrix, axisZ, new Vector3D(-0.05, 0, 0.9));
        drawVector(g, viewMatrix, axisZ, new Vector3D(0.05, 0, 0.9));
    }

    private void drawVector(Graphics g, Matrix4 viewMatrix, Vector3D from, Vector3D to) {

        Vector3D fromViewed = viewMatrix.multiply(from);
        Vector3D toViewed = viewMatrix.multiply(to);

        Vector2D fromProjected = projector.project(fromViewed);
        Vector2D toProjected = projector.project(toViewed);

        g.drawLine((int) fromProjected.x(), (int) fromProjected.y(), (int) toProjected.x(), (int) toProjected.y());
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
        mouseDeltaX = e.getX() - mouseStartX;
        mouseDeltaY = e.getY() - mouseStartY;
        this.mouseTotalX = mouseAccumX + mouseDeltaX;
        this.mouseTotalY = mouseAccumY + mouseDeltaY;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.mouseStartX = e.getX();
        this.mouseStartY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.mouseAccumX += mouseDeltaX;
        this.mouseAccumY += mouseDeltaY;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }
}
