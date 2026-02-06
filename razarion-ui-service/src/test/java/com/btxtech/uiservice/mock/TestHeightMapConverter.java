package com.btxtech.uiservice.mock;

import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.uiservice.terrain.HeightMapConverter;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Test implementation of HeightMapConverter for unit tests.
 * Simply calls toJavaArray() since there are no JSObject issues in tests.
 */
@Singleton
public class TestHeightMapConverter implements HeightMapConverter {

    @Inject
    public TestHeightMapConverter() {
    }

    @Override
    public int[] convert(Uint16ArrayEmu heightMap) {
        return heightMap.toJavaArray();
    }
}
