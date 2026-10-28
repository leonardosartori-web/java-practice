package interview;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.concurrent.*;

public class CartTest {

    @Test
    void calculateTotal() {
        Cart cart = new Cart();
        cart.addItem("1", new BigDecimal("12.5"), 3);
        assertEquals(new BigDecimal("37.5"), cart.total());
    }

    @Test
    void addTwoSameItem() {
        Cart cart = new Cart();
        cart.addItem("1", new BigDecimal("12.5"), 1);
        cart.addItem("1", new BigDecimal("12.5"), 2);
        assertEquals(new BigDecimal("37.5"), cart.total());
    }

    @Test
    void rejectsInvalidProductId() {
        Cart cart = new Cart();

        assertThrows(IllegalArgumentException.class, () -> {
            cart.addItem("", new BigDecimal("12.5"), 1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            cart.addItem("    ", new BigDecimal("12.5"), 1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            cart.addItem(null, new BigDecimal("12.5"), 1);
        });
    }

    @Test
    void rejectsInvalidUnitPrice() {
        Cart cart = new Cart();

        assertThrows(IllegalArgumentException.class, () -> {
            cart.addItem("1", new BigDecimal("-12.5"), 1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            cart.addItem("1", new BigDecimal("0"), 1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            cart.addItem("1", null, 1);
        });
    }

    @Test
    void rejectsInvalidQuantity() {
        Cart cart = new Cart();

        assertThrows(IllegalArgumentException.class, () -> {
            cart.addItem("1", new BigDecimal("12.5"), 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            cart.addItem("1", new BigDecimal("12.5"), -2);
        });

    }

    @Test
    void rejectsDifferentPriceSameProduct() {
        Cart cart = new Cart();
        cart.addItem("1", new BigDecimal("12.5"), 1);

        assertThrows(IllegalArgumentException.class, () -> {
            cart.addItem("1", new BigDecimal("12.6"), 1);
        });
    }


    @Test
    void concurrentAdds() throws Exception {
        Cart cart = new Cart();
        BigDecimal unitPrice = new BigDecimal("1.0");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Void> task = () -> {
            ready.countDown();
            start.await();

            for (int i = 0; i < 1000; i++) {
                cart.addItem("Item", unitPrice, 1);
            }
            return null;
        };

        try {
            Future<?> first = executorService.submit(task);
            Future<?> second = executorService.submit(task);

            ready.await(5, TimeUnit.SECONDS);
            start.countDown();

            first.get();
            second.get();

            assertEquals(0, cart.total().compareTo(new BigDecimal("2000")));
        } finally {
            executorService.shutdownNow();
        }

    }

}
