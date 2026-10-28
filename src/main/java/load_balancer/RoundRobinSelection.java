package load_balancer;

public class RoundRobinSelection implements SelectionStrategy {

    private int nextIndex = 0;

    @Override
    public int selectIndex(int numberOfAddresses) {
        int selectedIndex = nextIndex;

        nextIndex = (nextIndex + 1) % numberOfAddresses;

        return selectedIndex;
    }
}
