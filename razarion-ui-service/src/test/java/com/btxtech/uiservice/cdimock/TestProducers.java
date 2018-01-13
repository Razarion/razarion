package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.nativejs.NativeMatrixFactory;

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
        return null;
    }
}
