package com.btxtech.shared.gameengine.datatypes.itemtype;

import org.teavm.flavour.json.JsonPersistable;

/**
 * User: Razarion contributors
 * Date: 17.11.2009
 * Time: 23:18:42
 */
@JsonPersistable
public class HouseType {
    private int space;

    /**
     * Used by GWT
     */
    public HouseType() {
    }

    public void changeTo(HouseType houseType) {
        space = houseType.space;
    }

    public int getSpace() {
        return space;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    public HouseType space(int space) {
        setSpace(space);
        return this;
    }
}
