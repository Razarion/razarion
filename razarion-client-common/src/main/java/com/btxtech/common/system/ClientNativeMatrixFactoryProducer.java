package com.btxtech.common.system;

import com.btxtech.shared.nativejs.NativeMatrixFactory;

import javax.inject.Singleton;
import javax.enterprise.inject.Produces;

/**
 * Created by Beat
 * 28.03.2017.
 */
@Singleton
public class ClientNativeMatrixFactoryProducer {
    private NativeMatrixFactory nativeMatrixFactory = new NativeMatrixFactory() {};

    @Produces
    public NativeMatrixFactory getNativeMatrixFactory() {
        return nativeMatrixFactory;
    }
}
