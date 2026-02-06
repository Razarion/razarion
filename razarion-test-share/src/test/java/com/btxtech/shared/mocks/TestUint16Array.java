package com.btxtech.shared.mocks;

import com.btxtech.shared.datatypes.Uint16ArrayEmu;

public class TestUint16Array implements Uint16ArrayEmu {
    private final int[] intArray;

    public TestUint16Array(int[] intArray) {
        this.intArray = intArray;
    }

    @Override
    public int getAt(int i) {
        return intArray[i];
    }

    @Override
    public int getLength() {
        return intArray.length;
    }

    @Override
    public int[] toJavaArray() {
        return intArray.clone();
    }
}
