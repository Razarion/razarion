package com.btxtech.shared.datatypes.shape;

import java.util.List;

/**
 * Created by Beat
 * 28.07.2016.
 */
@Deprecated // Use ThreeJsModel
public class Shape3D {
    private int id;
    private List<Element3D> element3Ds;

    public int getId() {
        return id;
    }

    public List<Element3D> getElement3Ds() {
        return element3Ds;
    }

    public Shape3D id(int id) {
        this.id = id;
        return this;
    }

    public Shape3D element3Ds(List<Element3D> element3Ds) {
        this.element3Ds = element3Ds;
        return this;
    }
}
