import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadLocalRandom;

public class Generator implements Runnable {
    private static final AtomicInteger taskIdCounter = new AtomicInteger(1);
    private final ThreadPool pool;
    private volatile boolean isRunning = true;

    public Generator(ThreadPool pool) {
        this.pool = pool;
    }

    public void stopGenerator() {
        this.isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                int newTaskId = taskIdCounter.getAndIncrement();
                Task newTask = new Task(newTaskId);
                pool.submit(newTask);
                int sleepTimeMs = ThreadLocalRandom.current().nextInt(500, 5001);
                Thread.sleep(sleepTimeMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}