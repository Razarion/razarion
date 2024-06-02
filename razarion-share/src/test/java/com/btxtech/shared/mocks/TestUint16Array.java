package com.btxtech.shared.mocks;

import com.btxtech.shared.datatypes.Uint16ArrayEmu;

public class TestUint16Array implements Uint16ArrayEmu {
    private double[] doubles;

    public double[] getDoubles() {
        return doubles;
    }

    public void setDoubles(double[] doubles) {
        this.doubles = doubles;
    }

    public TestUint16Array doubles(double[] doubles) {
        setDoubles(doubles);
        return this;
    }
}
