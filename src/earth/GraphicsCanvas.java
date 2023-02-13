package earth;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class GraphicsCanvas extends Canvas implements Runnable {

    private final CanvasRenderer renderer;

    private Thread thread;

    private boolean isRunning = false;

    public GraphicsCanvas(CanvasRenderer renderer, int width, int height) {

        this.renderer = renderer;

        JFrame frame = new JFrame();
        this.setSize(width * 2, height);
        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }


    @Override
    public void run() {
        createBufferStrategy(2);
        BufferStrategy bs = this.getBufferStrategy();

        while (isRunning) {
            Graphics g = bs.getDrawGraphics();
            renderer.render(g);
            g.dispose();
            bs.show();
            Toolkit.getDefaultToolkit().sync();
        }
    }

    public void start() {
        if (isRunning)
            return;
        isRunning = true;

        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        if (!isRunning)
            return;
        isRunning = false;
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

}
