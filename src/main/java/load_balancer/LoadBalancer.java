package load_balancer;

import java.math.BigDecimal;
import java.util.*;

public class LoadBalancer {
    private final List<String> addresses = new ArrayList<>();
    private static final int MAX_ADDRESSES = 10;
    private final SelectionStrategy selectionStrategy;

    public LoadBalancer(SelectionStrategy selectionStrategy) {
        this.selectionStrategy = Objects.requireNonNull(selectionStrategy);
    }

    public LoadBalancer() {
        this(new RoundRobinSelection());
    }

    public synchronized void register(String address) {
        validateAddress(address);
        if (addresses.contains(address)) throw new IllegalArgumentException("Address already exists");
        if (size() >= MAX_ADDRESSES) throw new IllegalStateException("Reached max capacity");
        addresses.add(address);
        BigDecimal a = new BigDecimal(2.3);
    }

    private static void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address must not be null or blank");
        }
    }

    public synchronized String next() {
        if (addresses.isEmpty()) throw new IllegalStateException("No address registered");
        int index = selectionStrategy.selectIndex(addresses.size());
        return addresses.get(index);
    }

    public synchronized int size() { return addresses.size(); }
}
