package counter;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {

    private AtomicInteger value = new AtomicInteger(0);

    public void increment() {
        value.incrementAndGet();
    }

    public int getValue() {
        return value.get();
    }

}
