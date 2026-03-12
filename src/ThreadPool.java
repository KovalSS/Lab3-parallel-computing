import java.util.ArrayList;

public class ThreadPool {
    private final Queue taskQueue1;
    private final Queue taskQueue2;
    private final Object submitMonitor = new Object();
    private final ArrayList<Worker> workers;


    public ThreadPool() {
        taskQueue1 = new Queue();
        taskQueue2 = new Queue();
        workers = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            workers.add(new Worker(taskQueue1));
            workers.add(new Worker(taskQueue2));
        }
        for (Worker worker : workers) {
            worker.start();
        }
    }
    public void submit(Task task) {
        synchronized (submitMonitor) {
            int time1 = taskQueue1.getTotalTime();
            int time2 = taskQueue2.getTotalTime();

            if (time1 <= time2) {
                taskQueue1.addTask(task);
            } else {
                taskQueue2.addTask(task);
            }
        }
    }
    private class Worker extends Thread {
        private final Queue myQueue;

        public Worker(Queue queue) {
            this.myQueue = queue;
        }
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Task task = myQueue.getTask();
                    task.run();
                } catch (InterruptedException e) {
                    interrupt();
                    break;
                }
            }
        }
    }
}
