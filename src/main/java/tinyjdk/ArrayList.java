package tinyjdk;

public class ArrayList<T> implements List<T> {

    private Object[] a;
    private int sz;

    public ArrayList() {
        this.a = new Object[10];
    }

    @Override
    public T get(int i) {
        if (i >= 0 && i < size()) return (T) a[i];
        throw new RuntimeException("ArrayList.get(): index out of bounds");
    }

    @Override
    public T set(int i, T x) {
        if (i >= 0 && i < size()) {
            T old = get(i);
            a[i] = x;
            return old;
        }
        throw new RuntimeException("ArrayList.get(): index out of bounds");
    }

    @Override
    public void add(T x) {
        if (sz >= a.length) {
            Object[] old = a;
            a = new Object[2*a.length];
            for (int i = 0; i < old.length; i++) {
                a[i] = old[i];
            }
        }
        a[sz++] = x;
    }

    @Override
    public void clear() {
        sz = 0;
    }

    @Override
    public boolean contains(T x) {
        Iterator<T> it = iterator();
        while(it.hasNext()){
            T o = it.next();
            if (o.equals(x)) return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return sz == 0;
    }

    @Override
    public void remove(T x) {
        Iterator<T> it = iterator();
        int i = 0;
        while(it.hasNext()) {
            T o = it.next();
            i++;
            if (x.equals(o)) {
                while (i < size() - 1) {
                    set(i, get(i+1));
                }
                sz--;
            }
        }
    }

    @Override
    public int size() {
        return sz;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int pos = 0;
            @Override
            public boolean hasNext() {
                return (pos < size());
            }

            @Override
            public T next() {
                return get(pos++);
            }
        };
    }
}
