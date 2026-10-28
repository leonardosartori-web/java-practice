package interview;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {
    @Test
    void rejectsInvalidProductId() {
        Inventory inventory = new Inventory();
        assertThrows(IllegalArgumentException.class, () -> {
            inventory.addStock("", 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            inventory.addStock("    ", 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            inventory.addStock(null, 1);
        });
    }

    @Test
    void rejectsInvalidQuantity() {
        Inventory inventory = new Inventory();
        assertThrows(IllegalArgumentException.class, () -> {
            inventory.addStock("A", 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            inventory.addStock("SA", -155);
        });
    }

    @Test
    void notExistingProduct() {
        Inventory inventory = new Inventory();
        assertEquals(0, inventory.availableStock("A"));
    }

    @Test
    void rejectsReservationWithoutChangingStock() {
        Inventory inventory = new Inventory();
        inventory.addStock("book", 5);

        boolean reserved = inventory.reserve("book", 6);

        assertFalse(reserved);
        assertEquals(5, inventory.availableStock("book"));
    }

    @Test
    void onlyOneConcurrentReserveSucceed() throws Exception {
        Inventory inventory = new Inventory();
        inventory.addStock("book", 5);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> task = () -> {
            ready.countDown();
            start.await();
            return inventory.reserve("book", 4);
        };

        try {
            Future<Boolean> first = executorService.submit(task);
            Future<Boolean> second = executorService.submit(task);

            ready.await();
            start.countDown();

            boolean firstSucceed = first.get();
            boolean secondSucceed = second.get();

            assertNotEquals(firstSucceed, secondSucceed);
            assertEquals(1, inventory.availableStock("book"));
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void twoThreadsAddQuantityToSameProduct() throws Exception {
        Inventory inventory = new Inventory();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Void> task = () -> {
            ready.countDown();
            start.await();
            for (int i = 0; i < 1000; i++) {
                inventory.addStock("book", 1);
            }
            return null;
        };

        try {
            Future<Void> first = executorService.submit(task);
            Future<Void> second = executorService.submit(task);

            ready.await();
            start.countDown();

            first.get();
            second.get();
            assertEquals(2000, inventory.availableStock("book"));
        } finally {
            executorService.shutdownNow();
        }
    }

}
