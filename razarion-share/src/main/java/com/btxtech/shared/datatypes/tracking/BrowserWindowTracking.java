package com.btxtech.shared.datatypes.tracking;

import com.btxtech.shared.datatypes.Index;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class BrowserWindowTracking extends DetailedTracking {
    private Index dimension;

    public Index getDimension() {
        return dimension;
    }

    public BrowserWindowTracking setDimension(Index dimension) {
        this.dimension = dimension;
        return this;
    }
}
