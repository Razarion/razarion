package com.btxtech.server.service;

import java.util.function.Function;

public class PersistenceUtil {
    public static <E> Integer extractId(E e, Function<E, Integer> idSupplier) {
        if (e != null) {
            return idSupplier.apply(e);
        }
        return null;
    }

}
