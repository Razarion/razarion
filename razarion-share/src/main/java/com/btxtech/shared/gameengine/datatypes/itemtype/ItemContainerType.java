package com.btxtech.shared.gameengine.datatypes.itemtype;

import org.teavm.flavour.json.JsonPersistable;

import java.util.List;

/**
 * User: Razarion contributors
 * Date: 01.05.2010
 * Time: 10:54:25
 */
@JsonPersistable
public class ItemContainerType {
    private List<Integer> ableToContain;
    private int maxCount;
    private double range;

    public ItemContainerType setAbleToContain(List<Integer> ableToContain) {
        this.ableToContain = ableToContain;
        return this;
    }

    public List<Integer> getAbleToContain() {
        return ableToContain;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public ItemContainerType setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public ItemContainerType setRange(double range) {
        this.range = range;
        return this;
    }

    public double getRange() {
        return range;
    }

    public boolean isAbleToContain(int itemTypeId) {
        return ableToContain != null && ableToContain.contains(itemTypeId);
    }
}
