import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadPool {
    private final Queue taskQueue1;
    private final Queue taskQueue2;
    private final Object submitMonitor = new Object();
    private final ArrayList<Worker> workers;

    private volatile boolean isShutdown = false;
    private volatile boolean isPaused = false;
    private final Object pauseMonitor = new Object();

    private final AtomicLong totalThreadWaitTimeMs = new AtomicLong(0);
    private final AtomicInteger waitCycles = new AtomicInteger(0);
    private final AtomicLong totalTaskExecutionTimeMs = new AtomicLong(0);
    private final AtomicInteger completedTasksCount = new AtomicInteger(0);

    public ThreadPool() {
        taskQueue1 = new Queue();
        taskQueue2 = new Queue();
        workers = new ArrayList<>();

        for (int i = 1; i <= 2; i++) {
            workers.add(new Worker(taskQueue1, "Worker-Q1-" + i));
            workers.add(new Worker(taskQueue2, "Worker-Q2-" + i));
        }
        for (Worker worker : workers) {
            worker.start();
        }
    }

    public void submit(Task task) {
        if (isShutdown) {
            System.out.println("[REJECTED] Pool is shutting down. Task #" + task.getId() + " rejected.");
            return;
        }

        synchronized (submitMonitor) {
            int time1 = taskQueue1.getTotalTime();
            int time2 = taskQueue2.getTotalTime();

            boolean addToQueue1;

            if (time1 < time2) {
                addToQueue1 = true;
            } else if (time2 < time1) {
                addToQueue1 = false;
            } else {
                addToQueue1 = java.util.concurrent.ThreadLocalRandom.current().nextBoolean();
            }

            if (addToQueue1) {
                taskQueue1.addTask(task);
            } else {
                taskQueue2.addTask(task);
            }

            System.out.println("[SUBMIT] Task #" + task.getId() + " (" + task.getExecutionTime() + "s). " +
                    "Time Q1: " + time1 + "s, Time Q2: " + time2 + "s. -> Goes to Queue " + (addToQueue1 ? "1" : "2"));
        }
    }


    public void pause() {
        isPaused = true;
        System.out.println("\n=== THREAD POOL PAUSED ===\n");
    }

    public void resume() {
        synchronized (pauseMonitor) {
            isPaused = false;
            pauseMonitor.notifyAll();
            System.out.println("\n=== THREAD POOL RESUMED ===\n");
        }
    }

    public void shutdown() {
        System.out.println("\n=== GRACEFUL SHUTDOWN INITIATED ===");
        isShutdown = true;
        taskQueue1.stopWaiting();
        taskQueue2.stopWaiting();
        resume();
    }

    public void shutdownNow() {
        System.out.println("\n=== IMMEDIATE SHUTDOWN INITIATED ===");
        isShutdown = true;
        taskQueue1.clear();
        taskQueue2.clear();
        for (Worker worker : workers) {
            worker.interrupt();
        }
    }

    public void printMetrics() {
        System.out.println("\n====== SYSTEM METRICS ======");
        System.out.println("Total worker threads created: " + workers.size());

        double avgWaitTime = waitCycles.get() == 0 ? 0 : (double) totalThreadWaitTimeMs.get() / waitCycles.get();
        System.out.printf("Average thread waiting time: %.2f ms%n", avgWaitTime);

        System.out.printf("Average Queue 1 length: %.2f tasks%n", taskQueue1.getAverageSize());
        System.out.printf("Average Queue 2 length: %.2f tasks%n", taskQueue2.getAverageSize());

        double avgExecTime = completedTasksCount.get() == 0 ? 0 : (double) totalTaskExecutionTimeMs.get() / completedTasksCount.get() / 1000.0;
        System.out.printf("Average task execution time: %.2f seconds%n", avgExecTime);
        System.out.println("============================\n");
    }


    private class Worker extends Thread {
        private final Queue myQueue;

        public Worker(Queue queue, String name) {
            super(name);
            this.myQueue = queue;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (isPaused) {
                        synchronized (pauseMonitor) {
                            while (isPaused) {
                                pauseMonitor.wait();
                            }
                        }
                    }

                    long waitStart = System.currentTimeMillis();
                    Task task = myQueue.getTask();
                    long waitEnd = System.currentTimeMillis();

                    totalThreadWaitTimeMs.addAndGet(waitEnd - waitStart);
                    waitCycles.incrementAndGet();

                    if (task == null) break;

                    System.out.println("[" + getName() + "] started task #" + task.getId() + " (" + task.getExecutionTime() + "s).");

                    task.run();

                    totalTaskExecutionTimeMs.addAndGet(task.getExecutionTime() * 1000L);
                    completedTasksCount.incrementAndGet();

                    System.out.println("[" + getName() + "] finished task #" + task.getId() + ".");

                } catch (InterruptedException e) {
                    System.out.println("[" + getName() + "] WAS INTERRUPTED!");
                    interrupt();
                    break;
                }
            }
        }
    }
    public void awaitTermination() {
        for (Worker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("All workers have finished successfully.");
    }
}