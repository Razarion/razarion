package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 09.05.2016.
 */
public class PairHolder<T> {
    private final T o1;
    private final T o2;

    public PairHolder(T o1, T o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public T getO1() {
        return o1;
    }

    public T getO2() {
        return o2;
    }
}
