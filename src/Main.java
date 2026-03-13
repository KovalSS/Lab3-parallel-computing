import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

//        System.out.println("Starting test for 60 seconds...");
//        runTest(60);

        // System.out.println("Starting test for 150 seconds...");
        // runTest(150);

         System.out.println("Starting test for 300 seconds...");
         runTest(300);
    }

    private static void runTest(int testDurationSeconds) {
        ThreadPool pool = new ThreadPool();

        List<Generator> generators = new ArrayList<>();
        List<Thread> genThreads = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Generator gen = new Generator(pool);
            generators.add(gen);
            Thread t = new Thread(gen, "Generator-" + i);
            genThreads.add(t);
            t.start();
        }

        try {
            Thread.sleep(testDurationSeconds * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n--- TEST TIME IS UP. STOPPING GENERATORS... ---");
        for (Generator gen : generators) {
            gen.stopGenerator();
        }

        for (Thread t : genThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("--- GENERATORS STOPPED. WAITING FOR POOL TO FINISH TASKS... ---");
        pool.shutdown();

        pool.awaitTermination();

        pool.printMetrics();
    }
}