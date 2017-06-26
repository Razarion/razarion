package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 27.07.2016.
 */
public class DoubleHolder<T1, T2> {
    private T1 o1;
    private T2 o2;

    public DoubleHolder() {
    }

    public DoubleHolder(T1 o1, T2 o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public T1 getO1() {
        return o1;
    }

    public void setO1(T1 o1) {
        this.o1 = o1;
    }

    public T2 getO2() {
        return o2;
    }

    public void setO2(T2 o2) {
        this.o2 = o2;
    }
}
