package com.btxtech.shared.datatypes.tracking;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class BrowserWindowTracking extends DetailedTracking {
    private DecimalPosition dimension;

    public DecimalPosition getDimension() {
        return dimension;
    }

    public BrowserWindowTracking setDimension(DecimalPosition dimension) {
        this.dimension = dimension;
        return this;
    }
}
