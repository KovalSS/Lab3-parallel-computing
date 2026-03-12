import java.util.LinkedList;
public class Queue {
        private final java.util.Queue<Task> tasks;
        private volatile int totalTime = 0;
        public Queue() {
            this.tasks = new LinkedList<>();
        }

        public synchronized void addTask(Task task) {
            totalTime += task.getExecutionTime();
            tasks.offer(task);
            notify();
        }

        public synchronized Task getTask() throws InterruptedException {
            while (tasks.isEmpty()) {
                wait();
            }
            Task task = tasks.poll();
            totalTime -= task.getExecutionTime();

            return task;
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

        public  synchronized boolean isEmpty() {
            return tasks.isEmpty();
        }
}
