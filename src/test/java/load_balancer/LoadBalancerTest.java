package load_balancer;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class LoadBalancerTest {
    @Test
    void registerAnAddress() {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.register("Hi");
        assertEquals(loadBalancer.size(), 1);
    }

    @Test
    void rejectsDuplicates() {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.register("Hi");

        assertThrows(IllegalArgumentException.class,
                () -> {
                    loadBalancer.register("Hi");
                }
        );
    }

    @Test
    void rejectsNullAddress() {
        LoadBalancer loadBalancer = new LoadBalancer();

        assertThrows(IllegalArgumentException.class,
                () -> {
                    loadBalancer.register(null);
                }
        );
    }

    @Test
    void rejectsBlankAddress() {
        LoadBalancer loadBalancer = new LoadBalancer();

        assertThrows(IllegalArgumentException.class,
                () -> {
                    loadBalancer.register("");
                }
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> loadBalancer.register("   ")
        );

        assertEquals(0, loadBalancer.size());
    }

    @Test
    void rejectsNewAddressWhenFullCapacity() {
        LoadBalancer loadBalancer = new LoadBalancer();

        for (int i = 0; i < 10; i++) {
            loadBalancer.register("Hi" + i);
        }

        assertEquals(10, loadBalancer.size());

        assertThrows(IllegalStateException.class,
                () -> {
                    loadBalancer.register("Full");
                }
        );
    }

    @Test
    void synchronizeRegisterAccess() throws Exception {
        LoadBalancer loadBalancer = new LoadBalancer();

        int numberOfTasks = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfTasks);

        CountDownLatch ready = new CountDownLatch(numberOfTasks);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < 9; i++) {
            loadBalancer.register("Hi" + i);
        }

        try {
            List<Future<Boolean>> tasks = IntStream.range(0, numberOfTasks)
                    .mapToObj(address -> executorService.submit(() -> {
                        ready.countDown();
                        start.await();
                        try {
                            loadBalancer.register("new-" + address);
                            return true;
                        } catch (IllegalStateException e) {
                            return false;
                        }
                            }
                    )).toList();

            assertTrue(ready.await(2, TimeUnit.SECONDS));
            start.countDown();

            int successfulRegistrations = 0;

            for (Future<Boolean> task : tasks) {
                if (task.get(2, TimeUnit.SECONDS)) successfulRegistrations++;
            }

            assertEquals(1, successfulRegistrations);
            assertEquals(10, loadBalancer.size());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void selectAddressesInRoundRobin() {
        LoadBalancer loadBalancer = new LoadBalancer();
        for (String address: List.of("A", "B", "C")) {
            loadBalancer.register(address);
        }
        assertEquals("A", loadBalancer.next());
        assertEquals("B", loadBalancer.next());
        assertEquals("C", loadBalancer.next());
        assertEquals("A", loadBalancer.next());
    }

    @Test
    void rejectsSelectionWhenNoAddresses() {
        LoadBalancer loadBalancer = new LoadBalancer();

        assertThrows(IllegalStateException.class, () -> {
            loadBalancer.next();
        });
    }

    @Test
    void usesProvidedSelectionStrategy() {
        SelectionStrategy selectLast = size -> size - 1;

        LoadBalancer balancer = new LoadBalancer(selectLast);
        balancer.register("A");
        balancer.register("B");
        balancer.register("C");

        assertEquals("C", balancer.next());
        assertEquals("C", balancer.next());
    }

    @Test
    void foo() {
        BigDecimal f = new BigDecimal("10.0");
        BigDecimal s = new BigDecimal("10.00");
        System.out.println(f.equals(s));
        System.out.println(f.compareTo(s) == 0);
    }
}
