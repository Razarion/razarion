package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.nativejs.NativeMatrixFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Created by Beat
 * 28.03.2017.
 */
@ApplicationScoped
public class TestProducers {

    @Produces
    public NativeMatrixFactory getNativeMatrixFactory() {
        throw new UnsupportedOperationException();
    }
}