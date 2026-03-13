import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
//        testGracefulShutdownAndPause();

        testImmediateShutdown();


//        runTest(60);

        // runTest(150);

    }

    private static void runTest(int testDurationSeconds) {
        System.out.println("\n[TEST] Starting load test for " + testDurationSeconds + " seconds...");
        ThreadPool pool = new ThreadPool();

        List<Generator> generators = new ArrayList<>();
        List<Thread> genThreads = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Generator gen = new Generator(pool);
            generators.add(gen);
            Thread t = new Thread(gen, "Generator-Load-" + i);
            genThreads.add(t);
            t.start();
        }

        try {
            Thread.sleep(testDurationSeconds * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n[TEST] Time is up. Stopping generators...");
        for (Generator gen : generators) {
            gen.stopGenerator();
        }

        for (Thread t : genThreads) {
            try {
                t.interrupt(); // Миттєво перериваємо сон генераторів
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[TEST] Generators stopped. Initiating graceful shutdown to finish tasks...");
        pool.shutdown();
        pool.awaitTermination();
        pool.printMetrics();
    }

    private static void testGracefulShutdownAndPause() {
        System.out.println("\n======================================================");
        System.out.println(" SCENARIO 1: Pause, Resume, and Graceful Shutdown");
        System.out.println("======================================================");
        ThreadPool pool = new ThreadPool();
        List<Generator> generators = startGenerators(pool, 3);

        try {
            System.out.println("\n[TEST] Pool is operating in normal mode (10 seconds)...");
            Thread.sleep(10000);

            System.out.println("\n[TEST] Calling PAUSE...");
            pool.pause();
            System.out.println("[TEST] Pool is paused. Generators continue to add tasks, but workers should not take new ones.");
            System.out.println("[TEST] Waiting 15 seconds for current tasks to finish...");
            Thread.sleep(15000);

            System.out.println("\n[TEST] Calling RESUME...");
            pool.resume();
            System.out.println("[TEST] Pool is resumed. Workers should quickly process accumulated tasks.");
            Thread.sleep(10000);

            System.out.println("\n[TEST] Stopping generators...");
            stopGenerators(generators);

            System.out.println("[TEST] Calling SHUTDOWN (Graceful shutdown)...");
            pool.shutdown();

            pool.awaitTermination();
            pool.printMetrics();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void testImmediateShutdown() {
        System.out.println("\n======================================================");
        System.out.println(" SCENARIO 2: Hard (Immediate) Shutdown");
        System.out.println("======================================================");
        ThreadPool pool = new ThreadPool();
        List<Generator> generators = startGenerators(pool, 3);

        try {
            System.out.println("\n[TEST] Pool is operating in normal mode (10 seconds)...");
            Thread.sleep(10000);

            System.out.println("\n[TEST] Stopping generators...");
            stopGenerators(generators);

            System.out.println("\n[TEST] Calling SHUTDOWN NOW (Immediate shutdown)...");
            pool.shutdownNow();

            pool.awaitTermination();

            System.out.println("[TEST] Pool emergency stopped. Metrics will be incomplete due to interruption.");
            pool.printMetrics();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static List<Generator> startGenerators(ThreadPool pool, int count) {
        List<Generator> generators = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Generator gen = new Generator(pool);
            generators.add(gen);
            new Thread(gen, "Generator-Test-" + i).start();
        }
        return generators;
    }

    private static void stopGenerators(List<Generator> generators) {
        for (Generator gen : generators) {
            gen.stopGenerator();
        }
    }
}