package load_balancer;

public interface SelectionStrategy {

    int selectIndex(int numberOfAddresses);
}
