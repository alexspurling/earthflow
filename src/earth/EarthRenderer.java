package earth;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class EarthRenderer implements CanvasRenderer, KeyListener {


    public static final int WIDTH = 1024;
    public static final int HEIGHT = 1024;

    private static final double FOV = 0.62;
    public static final double RAY_Z = Math.tan(Math.toRadians(90 - (FOV / 2)));

    public static final int MAX_TIME_SPEED = (int) Math.pow(2, 20);

    private final BufferedImage canvas;
    private final Sphere sphere;
    private final EarthImageLoader loader;
    private final EarthTextureCache cache;

    private Texture earthTexture1;
    private Texture earthTexture2;

    private int frameCount = 0;
    private long lastFpsTime = System.currentTimeMillis();

//    private Vector3D camera = new Vector3D(0, 0.5, -0.5);
    private final Vector3D camera = new Vector3D(0, 0, 0);

    private final Vector3D lightDirection = new Vector3D(0, 0, 1);

    private long lastFrameTime = System.nanoTime();
    private final Set<Integer> pressedKeys = new HashSet<>();

    private OffsetDateTime dateTime = OffsetDateTime.of(2023, 1, 19, 0, 3, 42, 0, ZoneOffset.UTC);

    private int timeSpeed = 1;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private int fps;

    private String status = "";

    public EarthRenderer() {

        canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        double distance = Math.sqrt(Math.pow(513256.302301, 2) + Math.pow(-1132637.821089, 2) + Math.pow(-676524.885803, 2));

        sphere = new Sphere(new Vector3D(0, 0, distance), 6378);
        loader = new EarthImageLoader();

        long startTime = System.currentTimeMillis();

        System.out.println("Loaded textures in " + (System.currentTimeMillis() - startTime) + " ms");

        cache = new EarthTextureCache(sphere, loader);
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

        dt = 45;
//        System.out.println(dt);

        dateTime = dateTime.plus((int) (dt * timeSpeed), ChronoUnit.MILLIS);

        cache.update(dateTime);

        earthTexture1 = cache.getTextureBefore(dateTime);
        earthTexture2 = cache.getTextureAfter(dateTime);

        g.setColor(new Color(123, 234, 12));
        g.drawImage(canvas, 0, 0, null);
        g.drawImage(earthTexture1.getTexture(), WIDTH, 0, WIDTH * 4, HEIGHT * 2, null);
//        g.drawImage(earthTexture2.getTexture(), WIDTH, 500, 1000, 500, null);

        g.drawString("FPS: " + fps, 20, 20);
        g.drawString(String.format("Date: " + DATE_TIME_FORMATTER.format(dateTime)), 20, 40);
        g.drawString(String.format("Speed: " + timeSpeed), 20, 60);
        g.drawString(cache.getStatus(), 20, 80);

        sphere.update(dateTime);

        long secondsSinceImage1 = ChronoUnit.SECONDS.between(earthTexture1.getDate(), dateTime);
        long secondsBetweenImages = ChronoUnit.SECONDS.between(earthTexture1.getDate(), earthTexture2.getDate());
        double blend = (double) secondsSinceImage1 / secondsBetweenImages;

        // Render sphere using generated earth texture
        IntStream.range(0, WIDTH).parallel().forEach((x) -> {
//        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                // Reset the colour of each pixel
                canvas.setRGB(x, y, 0);

                double x3d = (double) x / (WIDTH / 2.0) - 1;
                double y3d = (double) (HEIGHT - y) / (HEIGHT / 2.0) - 1;
                Vector3D ray = new Vector3D(x3d, y3d, RAY_Z);

                Intersection intersection = sphere.getIntersection(ray, camera);

                if (intersection == null) continue;

                double normalDotCamera = intersection.normal().dot(intersection.point().subtract(camera));
                if (normalDotCamera > 0) continue;

                double normalDotLight = intersection.normal().dot(lightDirection);
                if (normalDotLight >= -1 && normalDotLight <= 0) {

                    Color texColour = sphere.getTextureColour(intersection.point(),
                            earthTexture1.getTexture(), earthTexture2.getTexture(), blend);
//                    Color litColour = multiplyColour(texColour, -normalDotLight);
                    canvas.setRGB(x, y, texColour.getRGB());
                }
            }
        });

        frameCount++;
        long time = System.currentTimeMillis();
        if (time - lastFpsTime > 1000) {
            fps = frameCount;
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

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!pressedKeys.contains(e.getKeyCode())) {
            pressedKeys.add(e.getKeyCode());
            adjustTimeSpeed(e);
//            download(e);
        }
    }

//    private void download(KeyEvent e) {
//        if (e.getKeyCode() == KeyEvent.VK_K) {
//            long startTime = System.currentTimeMillis();
//            status = "Downloading images for timestamp " + DATE_TIME_FORMATTER.format(dateTime);
//            System.out.println("Downloading images for timestamp " + dateTime);
//            List<EarthImage> earthImages = loader.getEarthImages(dateTime);
//            long timeTaken = System.currentTimeMillis() - startTime;
//            status = "Loaded " + earthImages.size() + " images in " + timeTaken + "ms";
//            System.out.println("Loaded " + earthImages.size() + " images in " + timeTaken + "ms");
//
//
//            startTime = System.currentTimeMillis();
//            List<EarthTexture> earthTextures = earthImages.stream().map(i -> new EarthTexture(sphere, i)).toList();
//            timeTaken = System.currentTimeMillis() - startTime;
//            status = "Loaded " + earthTextures.size() + " textures in " + timeTaken + "ms";
//            System.out.println("Loaded " + earthTextures.size() + " textures in " + timeTaken + "ms");
//            if (!earthTextures.isEmpty()) {
//                earthTexture = earthTextures.get(0).getEarthTexture();
//            }
//        }
//    }

    private void adjustTimeSpeed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_L) {
            if (timeSpeed >= 1 && timeSpeed < MAX_TIME_SPEED) {
                timeSpeed = timeSpeed * 2;
            } else if (timeSpeed == -1 || timeSpeed == 0) {
                timeSpeed = 1;
            } else if (timeSpeed < -1){
                timeSpeed = timeSpeed / 2;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_J) {
            if (timeSpeed > 1) {
                timeSpeed = timeSpeed / 2;
            } else if (timeSpeed == 1 || timeSpeed == 0) {
                timeSpeed = -1;
            } else if (timeSpeed > -MAX_TIME_SPEED) {
                timeSpeed = timeSpeed * 2;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_K) {
            timeSpeed = 0;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }
}
