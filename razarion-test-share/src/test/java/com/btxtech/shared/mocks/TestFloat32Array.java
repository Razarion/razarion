package com.btxtech.shared.mocks;

import com.btxtech.shared.datatypes.Float32ArrayEmu;

public class TestFloat32Array implements Float32ArrayEmu {
    private double[] doubles;

    public double[] getDoubles() {
        return doubles;
    }

    public void setDoubles(double[] doubles) {
        this.doubles = doubles;
    }

    public TestFloat32Array doubles(double[] doubles) {
        setDoubles(doubles);
        return this;
    }
}
