package transferservice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TransferService {

    private static final class Account {
        private final String id;
        private BigDecimal balance;
        private final ReentrantLock lock = new ReentrantLock();

        private Account(String id, BigDecimal balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicLong successfulTransfers = new AtomicLong();

    public void openAccount(String accountId, BigDecimal balance) {

        Account newAccount = new Account(accountId, balance);
        Account exisingAccount = accounts.putIfAbsent(accountId, newAccount);

        if (exisingAccount != null) throw new IllegalArgumentException("AccountId already registered");

    }

    public BigDecimal balanceOf(String accountId) {
        Account account = requireAccount(accountId);
        account.lock.lock();
        try {
            return account.balance;
        } finally {
            account.lock.unlock();
        }
    }

    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) {

        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");

        if (fromAccountId.equals(toAccountId)) throw new IllegalArgumentException("FromAccount and ToAccount must be different");

        if (!accounts.containsKey(fromAccountId)) throw new IllegalArgumentException("FromAccountId not existing");
        if (!accounts.containsKey(toAccountId)) throw new IllegalArgumentException("ToAccountId not existing");

        Account fromAccount = requireAccount(fromAccountId);
        Account toAccount = requireAccount(toAccountId);

        Account firstLock;
        Account secondLock;

        if (fromAccount.id.compareTo(toAccount.id) < 0) {
            firstLock = fromAccount;
            secondLock = toAccount;
        } else {
            firstLock = toAccount;
            secondLock = fromAccount;
        }

        firstLock.lock.lock();
        secondLock.lock.lock();

        try {
            if (fromAccount.balance.compareTo(amount) < 0) throw new IllegalStateException("Insufficient balance");

            fromAccount.balance = fromAccount.balance.subtract(amount);
            toAccount.balance = toAccount.balance.add(amount);
            successfulTransfers.incrementAndGet();

        } finally {
            firstLock.lock.unlock();
            secondLock.lock.unlock();
        }

    }

    private Account requireAccount(String accountId) {
        Account account = accounts.get(accountId);

        if (account == null) {
            throw new IllegalArgumentException(
                    "Unknown account: " + accountId
            );
        }
        return account;
    }

    public long successfulTransferCount() {
        return successfulTransfers.get();
    }

}
