package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 27.07.2016.
 */
public class SingleHolder<T> {
    private T o;

    public T getO() {
        return o;
    }

    public void setO(T o) {
        this.o = o;
    }

    public boolean isEmpty() {
        return o == null;
    }
}
