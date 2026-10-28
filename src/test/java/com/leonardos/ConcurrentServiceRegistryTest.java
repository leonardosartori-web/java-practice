package com.leonardos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import service_registry.ConcurrentServiceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ConcurrentServiceRegistryTest {
    @Test
    void removesRegisteredService() {
        ConcurrentServiceRegistry registry = new ConcurrentServiceRegistry();
        registry.register("payments", "https://payments.example");

        boolean removed = registry.unregister("payments");

        assertTrue(removed);
        assertEquals(Optional.empty(), registry.find("payments"));
        assertEquals(0, registry.size());
    }

    @Test
    void onlyOneServiceRemovesService() throws Exception {
        ConcurrentServiceRegistry registry = new ConcurrentServiceRegistry();
        registry.register("payments", "https://payments.example");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> unregister = () -> {
            ready.countDown();
            start.await();
            return registry.unregister("payments");
        };

        try {
            Future<Boolean> first = executor.submit(unregister);
            Future<Boolean> second = executor.submit(unregister);

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            boolean firstResult = first.get(5, TimeUnit.SECONDS);
            boolean secondResult = second.get(5, TimeUnit.SECONDS);

            System.out.println("First: " + firstResult);
            System.out.println("Second: " + secondResult);

            assertNotEquals(firstResult, secondResult);
            assertEquals(0, registry.size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void addWhetherFullCapacity() throws Exception {
        ConcurrentServiceRegistry registry = new ConcurrentServiceRegistry();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> task = () -> {
            ready.countDown();
            start.await();
            try {
                registry.register(Thread.currentThread().getName(), "https://payments.example");
                return true;
            } catch (IllegalStateException e) {
                return false;
            }
        };

        try {
            Future<Boolean> first = executor.submit(task);
            Future<Boolean> second = executor.submit(task);

            ready.await();
            start.countDown();

            System.out.println(first.get());
            System.out.println(second.get());

            assertEquals(1, registry.size());

        } finally {
            executor.shutdownNow();
        }
    }

}
