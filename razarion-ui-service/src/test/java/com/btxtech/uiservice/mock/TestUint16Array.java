package com.btxtech.uiservice.mock;

import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.heightToUnit16;

public class TestUint16Array implements Uint16ArrayEmu {
    private final int[] intArray;

    public TestUint16Array(int[] intArray) {
        this.intArray = intArray;
    }

    @Override
    public int getAt(int i) {
        if(i / TerrainUtil.NODE_X_COUNT > 80) {
            return heightToUnit16(1);
        } else {
            return heightToUnit16(-1);
        }
    }

    @Override
    public int getLength() {
        return (TerrainUtil.NODE_X_COUNT + 1) * (TerrainUtil.NODE_Y_COUNT + 1);
    }

    @Override
    public int[] toJavaArray() {
        int size = getLength();
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = getAt(i);
        }
        return result;
    }
}
