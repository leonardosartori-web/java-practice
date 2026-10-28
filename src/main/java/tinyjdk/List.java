package tinyjdk;

public interface List<T> extends Collection<T> {
    T get(int i) throws Exception;
    T set(int i, T x);
}
