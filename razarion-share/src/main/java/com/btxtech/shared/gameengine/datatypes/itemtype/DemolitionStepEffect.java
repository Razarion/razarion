package com.btxtech.shared.gameengine.datatypes.itemtype;

import java.util.List;

/**
 * Created by Beat
 * 20.12.2016.
 */
public class DemolitionStepEffect {
    private List<DemolitionShape3D> demolitionShape3Ds;

    public List<DemolitionShape3D> getDemolitionShape3Ds() {
        return demolitionShape3Ds;
    }

    public DemolitionStepEffect setDemolitionShape3Ds(List<DemolitionShape3D> demolitionShape3Ds) {
        this.demolitionShape3Ds = demolitionShape3Ds;
        return this;
    }
}
