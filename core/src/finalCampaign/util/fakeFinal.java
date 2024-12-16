package finalCampaign.util;

public class fakeFinal<T> {
    private T object;

    public fakeFinal() {}

    public fakeFinal(T v) {
        object = v;
    }

    public void set(T obj) {
        object = obj;
    }

    public T get() {
        return object;
    }
}
