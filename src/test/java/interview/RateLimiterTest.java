package interview;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RateLimiterTest {
    @Test
    void resetsWindowAtExpiration() {
        RateLimiter limiter = new RateLimiter(2, 1000);

        assertTrue(limiter.allowRequest("alice", 0));
        assertTrue(limiter.allowRequest("alice", 100));
        assertFalse(limiter.allowRequest("alice", 900));

        assertTrue(limiter.allowRequest("alice", 1000));
    }

    @Test
    void enforcesLimitAndResetsCounter() {
        RateLimiter limiter = new RateLimiter(2, 1000);

        assertTrue(limiter.allowRequest("alice", 0));
        assertTrue(limiter.allowRequest("alice", 100));

        assertFalse(limiter.allowRequest("alice", 200));
        assertFalse(limiter.allowRequest("alice", 999));

        assertTrue(limiter.allowRequest("alice", 1000));
        assertTrue(limiter.allowRequest("alice", 1100));
        assertFalse(limiter.allowRequest("alice", 1200));
    }

    @Test
    void keepsSeparateWindowsForEachClient() {
        RateLimiter limiter = new RateLimiter(1, 1000);

        assertTrue(limiter.allowRequest("alice", 0));
        assertFalse(limiter.allowRequest("alice", 100));

        // Alice ha esaurito il limite, ma Bob può ancora accedere.
        assertTrue(limiter.allowRequest("bob", 500));

        assertTrue(limiter.allowRequest("alice", 1000));

        // Per Bob sono trascorsi solo 500 ms: la sua finestra è ancora valida.
        assertFalse(limiter.allowRequest("bob", 1000));
        assertTrue(limiter.allowRequest("bob", 1500));
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> new RateLimiter(0, 1000));

        assertThrows(IllegalArgumentException.class,
                () -> new RateLimiter(-1, 1000));

        assertThrows(IllegalArgumentException.class,
                () -> new RateLimiter(2, 0));

        assertThrows(IllegalArgumentException.class,
                () -> new RateLimiter(2, -1));
    }

    @Test
    void rejectsInvalidRequestsWithoutConsumingQuota() {
        RateLimiter limiter = new RateLimiter(1, 1000);

        assertThrows(IllegalArgumentException.class,
                () -> limiter.allowRequest(null, 0));

        assertThrows(IllegalArgumentException.class,
                () -> limiter.allowRequest("", 0));

        assertThrows(IllegalArgumentException.class,
                () -> limiter.allowRequest("   ", 0));

        assertThrows(IllegalArgumentException.class,
                () -> limiter.allowRequest("alice", -1));

        // La richiesta con tempo negativo non deve aver consumato il limite.
        assertTrue(limiter.allowRequest("alice", 0));
    }


    @Test
    void respectsLimitsUnderConcurrentRequests() throws Exception {
        RateLimiter rateLimiter = new RateLimiter(3, 1000);
        int numberOfTasks = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfTasks);

        CountDownLatch ready = new CountDownLatch(numberOfTasks);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Boolean>> tasks = IntStream.range(0, numberOfTasks).mapToObj(i ->
                executorService.submit(() -> {
                    ready.countDown();
                    start.await();
                    return rateLimiter.allowRequest("alice", 0);
                })).toList();

            ready.await();
            start.countDown();

            int accepted = 0;

            for (Future<Boolean> task : tasks) {
                if (task.get()) accepted++;
            }

            assertEquals(3, accepted);
            assertFalse(rateLimiter.allowRequest("alice", 0));

        } finally {
            executorService.shutdownNow();
        }
    }
}
