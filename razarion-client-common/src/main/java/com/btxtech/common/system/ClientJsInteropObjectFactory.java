package com.btxtech.common.system;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.JsInteropObjectFactory;
import elemental2.core.Float32Array;
import jsinterop.base.Js;

import javax.inject.Singleton;
import java.util.List;

import static com.btxtech.shared.utils.CollectionUtils.toArray;

/**
 * Created by Beat
 * 01.04.2017.
 */
@Singleton
public class ClientJsInteropObjectFactory implements JsInteropObjectFactory {
    @Override
    public Float32ArrayEmu newFloat32Array4Vertices(List<Vertex> vertices) {
        return Js.uncheckedCast(new Float32Array(Vertex.toArray(vertices)));
    }

    @Override
    public Float32ArrayEmu newFloat32Array4DecimalPositions(List<DecimalPosition> decimalPositions) {
        return Js.uncheckedCast(new Float32Array(DecimalPosition.toArray(decimalPositions)));
    }

    @Override
    public Float32ArrayEmu newFloat32Array4Doubles(List<Double> doubles) {
        return Js.uncheckedCast(new Float32Array(toArray(doubles)));
    }
}
