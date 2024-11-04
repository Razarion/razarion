package com.btxtech.shared.mocks;

import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import elemental2.core.Uint16Array;

public class TestUint16Array extends Uint16Array implements Uint16ArrayEmu {
    private final int[] intArray;

    public TestUint16Array(int[] intArray) {
        super(0);
        this.intArray = intArray;
    }

    @Override
    public Double getAt(int i) {
        return (double)intArray[i];
    }


}
