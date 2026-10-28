package transferservice;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.concurrent.*;

public class TransferServiceTest {
    @Test
    void openAccountWithInitialBalance() {
        TransferService service = new TransferService();
        service.openAccount("alice", new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), service.balanceOf("alice"));
    }

    @Test
    void rejectsDuplicateAccount() {
        TransferService service = new TransferService();
        service.openAccount("alice", new BigDecimal("100.00"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.openAccount("alice", new BigDecimal("200.00"))
        );
        assertEquals(new BigDecimal("100.00"), service.balanceOf("alice"));
    }

    @Test
    void transferMoneyBetweenAccounts() {
        TransferService service = new TransferService();
        service.openAccount("alice", new BigDecimal("100.00"));
        service.openAccount("bob", new BigDecimal("50.00"));

        service.transfer(
                "alice",
                "bob",
                new BigDecimal("30.00")
        );

        assertEquals(
                new BigDecimal("70.00"),
                service.balanceOf("alice")
        );

        assertEquals(
                new BigDecimal("80.00"),
                service.balanceOf("bob")
        );
    }

    @Test
    void rejectsTransferDueToInsufficientBalance() {
        TransferService service = new TransferService();
        service.openAccount("alice", new BigDecimal("20.00"));
        service.openAccount("bob", new BigDecimal("50.00"));

        assertThrows(IllegalStateException.class, () -> {
            service.transfer(
                    "alice",
                    "bob",
                    new BigDecimal("30.00")
            );
        });

        assertEquals(
                new BigDecimal("20.00"),
                service.balanceOf("alice")
        );

        assertEquals(
                new BigDecimal("50.00"),
                service.balanceOf("bob")
        );
    }

    @Test
    void rejectsTransferToUnknownAccounts() {
        TransferService service = new TransferService();
        service.openAccount("alice", new BigDecimal("20.00"));

        assertThrows(IllegalArgumentException.class, () -> {
            service.transfer(
                    "alice",
                    "unknown",
                    new BigDecimal("30.00")
            );
        });

        assertEquals(
                new BigDecimal("20.00"),
                service.balanceOf("alice")
        );
    }

    @Test
    void rejectsTransferBetweenSameAccount() {
        TransferService service = new TransferService();
        service.openAccount("alice", new BigDecimal("20.00"));

        assertThrows(IllegalArgumentException.class, () -> {
            service.transfer(
                    "alice",
                    "alice",
                    new BigDecimal("30.00")
            );
        });

        assertEquals(
                new BigDecimal("20.00"),
                service.balanceOf("alice")
        );
    }

    @Test
    void rejectsTransferWithNegativeAmount() {
        TransferService service = new TransferService();
        service.openAccount("alice", new BigDecimal("20.00"));
        service.openAccount("bob", new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class, () -> {
            service.transfer(
                    "alice",
                    "bob",
                    null
            );
        });

        assertEquals(
                new BigDecimal("20.00"),
                service.balanceOf("alice")
        );

        assertEquals(
                new BigDecimal("50.00"),
                service.balanceOf("bob")
        );
    }

    @Test
    void concurrentMoneyTransfer() throws Exception {
        TransferService service = new TransferService();
        service.openAccount("alice", new BigDecimal("100.00"));
        service.openAccount("bob", new BigDecimal("0.00"));
        service.openAccount("charlie", new BigDecimal("0.00"));

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Boolean> first = executorService.submit(
                    () -> {
                        ready.countDown();
                        start.await();
                        try {
                            service.transfer("alice", "bob", new BigDecimal("80.00"));
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
            );

            Future<Boolean> second = executorService.submit(
                    () -> {
                        ready.countDown();
                        start.await();
                        try {
                            service.transfer("alice", "charlie", new BigDecimal("80.00"));
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
            );

            ready.await();
            start.countDown();

            assertNotEquals(first.get(), second.get());

        } finally {
            executorService.shutdownNow();
        }

    }


    @Test
    void countsOnlySuccessfulTransfers() {
        TransferService service = new TransferService();

        service.openAccount("alice", new BigDecimal("100.00"));
        service.openAccount("bob", new BigDecimal("50.00"));

        service.transfer(
                "alice",
                "bob",
                new BigDecimal("10.00")
        );

        service.transfer(
                "bob",
                "alice",
                new BigDecimal("5.00")
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.transfer(
                        "alice",
                        "bob",
                        new BigDecimal("1000.00")
                )
        );

        assertEquals(2, service.successfulTransferCount());
    }

}
