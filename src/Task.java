import java.util.concurrent.ThreadLocalRandom;
public class Task implements Runnable{
    private final static int MAX_EXECUTION_TIME = 15;
    private final static int MIN_EXECUTION_TIME = 2;

    private final int id;
    private final int executionTime;
    public Task(int id) {
        this.id = id;
        this.executionTime = ThreadLocalRandom.current().nextInt(MIN_EXECUTION_TIME, MAX_EXECUTION_TIME + 1);
    }
    public int getId() {
        return id;
    }
    public int getExecutionTime() {
        return executionTime;
    }
    @Override
    public String toString() {
        return "Task{" + "id=" + id +", executionTime=" + executionTime +'}';
    }

    @Override
    public void run() {
        try {
            Thread.sleep(executionTime * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
