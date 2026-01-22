package com.btxtech.uiservice.mock;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.JsInteropObjectFactory;

import jakarta.inject.Singleton;
import java.util.List;

/**
 * Created by Beat
 * 01.04.2017.
 */
@Singleton
public class TestJsInteropObjectFactory implements JsInteropObjectFactory {
    @Override
    public Float32ArrayEmu newFloat32Array4Vertices(List<Vertex> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Float32ArrayEmu newFloat32Array4DecimalPositions(List<DecimalPosition> decimalPositions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Float32ArrayEmu newFloat32Array4Doubles(List<Double> doubles) {
        throw new UnsupportedOperationException();
    }


}
