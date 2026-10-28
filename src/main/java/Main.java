import java.util.concurrent.*;

public class Main {

    public static Boolean foo() {
        return true;
    }

    public static void concurrentThreads() {

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch readyThreads = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> task = () -> {
            readyThreads.countDown();
            start.await();
            return foo();
        };

        try {
            Future<Boolean> first = executorService.submit(task);
            Future<Boolean> second = executorService.submit(task);

            start.countDown();
            System.out.println(first.get());
            System.out.println(second.get());


        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdownNow();
        }
    }

}