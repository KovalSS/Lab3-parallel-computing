import java.util.LinkedList;

public class Queue {
    private final java.util.Queue<Task> tasks;
    private volatile int totalTime = 0;
    private volatile boolean isStopped = false;

    private long sumOfSizes = 0;
    private int sizeMeasurements = 0;

    public Queue() {
        this.tasks = new LinkedList<>();
    }

    public synchronized void stopWaiting() {
        isStopped = true;
        notifyAll();
    }

    public synchronized void addTask(Task task) {
        totalTime += task.getExecutionTime();
        tasks.offer(task);
        recordSizeMetric();
        notify();
    }

    public synchronized Task getTask() throws InterruptedException {
        while (tasks.isEmpty()) {
            if (isStopped) return null;
            wait();
        }
        Task task = tasks.poll();
        totalTime -= task.getExecutionTime();
        recordSizeMetric();
        return task;
    }

    private void recordSizeMetric() {
        sumOfSizes += tasks.size();
        sizeMeasurements++;
    }

    public synchronized double getAverageSize() {
        return sizeMeasurements == 0 ? 0 : (double) sumOfSizes / sizeMeasurements;
    }

    public synchronized int getSize() {
        return tasks.size();
    }

    public int getTotalTime() {
        return totalTime;
    }

    public synchronized void clear() {
        tasks.clear();
        totalTime = 0;
    }

    public synchronized boolean isEmpty() {
        return tasks.isEmpty();
    }
}