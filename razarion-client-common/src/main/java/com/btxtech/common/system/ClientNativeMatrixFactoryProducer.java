package com.btxtech.common.system;

import com.btxtech.shared.nativejs.NativeMatrixFactory;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 28.03.2017.
 */
@Singleton
public class ClientNativeMatrixFactoryProducer {
    private NativeMatrixFactory nativeMatrixFactory = new NativeMatrixFactory() {};

    // TODO  @Produces
    public NativeMatrixFactory getNativeMatrixFactory() {
        return nativeMatrixFactory;
    }
}
