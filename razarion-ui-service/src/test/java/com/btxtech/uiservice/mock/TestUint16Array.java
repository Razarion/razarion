package com.btxtech.uiservice.mock;

import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import elemental2.core.Uint16Array;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.heightToUnit16;

public class TestUint16Array extends Uint16Array implements Uint16ArrayEmu {
    private final int[] intArray;

    public TestUint16Array(int[] intArray) {
        super(0);
        this.intArray = intArray;
    }

    @Override
    public Double getAt(int i) {
        if(i / TerrainUtil.NODE_X_COUNT > 80) {
            return (double) heightToUnit16(1);
        } else {
            return (double) heightToUnit16(-1);
        }
    }


}
