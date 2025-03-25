package com.btxtech.shared.system;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;

import java.util.List;

/**
 * Created by Beat
 * 01.04.2017.
 */
public interface JsInteropObjectFactory {
    Float32ArrayEmu newFloat32Array4Vertices(List<Vertex> vertices);

    Float32ArrayEmu newFloat32Array4DecimalPositions(List<DecimalPosition> decimalPositions);

    Float32ArrayEmu newFloat32Array4Doubles(List<Double> doubles);
}
