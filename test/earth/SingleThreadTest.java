package earth;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SingleThreadTest {

    @Test
    public void testSingleThreadedExecutor() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    long time = (long) (Math.random() * 100);
                    Thread.sleep(time);
                    System.out.println("Finished " + finalI + " after " + time + "ms");
                } catch (InterruptedException e) {
                    //
                }
            });
        }

        System.out.println("Waiting for tasks to complete");

        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}
