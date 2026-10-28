package com.leonardos;

import counter.Counter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcurrentSyncTest {
    @Test
    void synchronizationIncrement() {
        Counter counter = new Counter();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) {
                counter.increment();
            }
        };

        try {
            Future<?> first = executorService.submit(task);
            Future<?> second = executorService.submit(task);

            first.get();
            second.get();

            System.out.println(counter.getValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdownNow();
        }

    }
}
