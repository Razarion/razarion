package com.btxtech.shared.cdimock;

import com.btxtech.shared.TestTerrainTileObjectList;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileObjectList;
import com.btxtech.shared.mocks.TestFloat32Array;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.inject.Singleton;
import java.util.List;

import static com.btxtech.shared.utils.CollectionUtils.toArray;

/**
 * Created by Beat
 * 01.04.2017.
 */
@Singleton
public class TestJsInteropObjectFactory implements JsInteropObjectFactory {
    @Override
    public TerrainTileObjectList generateTerrainTileObjectList() {
        return new TestTerrainTileObjectList();
    }

    @Override
    public Float32ArrayEmu newFloat32Array4Vertices(List<Vertex> vertices) {
        return new TestFloat32Array().doubles(Vertex.toArray(vertices));
    }

    @Override
    public Float32ArrayEmu newFloat32Array4DecimalPositions(List<DecimalPosition> decimalPositions) {
        return new TestFloat32Array().doubles(DecimalPosition.toArray(decimalPositions));
    }

    @Override
    public Float32ArrayEmu newFloat32Array4Doubles(List<Double> doubles) {
        return new TestFloat32Array().doubles(toArray(doubles));
    }


}
