package load_balancer;

import java.util.concurrent.ThreadLocalRandom;

public class RandomSelection implements SelectionStrategy {
    @Override
    public int selectIndex(int numberOfAddresses) {
        return ThreadLocalRandom.current().nextInt(numberOfAddresses);
    }
}
