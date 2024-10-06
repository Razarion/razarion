package com.btxtech.shared.datatypes;

import org.dominokit.jackson.annotation.JSONMapper;

import java.util.Map;

/**
 * Created by Beat
 * on 24.09.2017.
 */
@JSONMapper
public class UnlockedItemPacket {
    private Map<Integer, Integer> unlockedItemLimit;
    private boolean availableUnlocks;

    public Map<Integer, Integer> getUnlockedItemLimit() {
        return unlockedItemLimit;
    }

    public void setUnlockedItemLimit(Map<Integer, Integer> unlockedItemLimit) {
        this.unlockedItemLimit = unlockedItemLimit;
    }

    public boolean isAvailableUnlocks() {
        return availableUnlocks;
    }

    public void setAvailableUnlocks(boolean availableUnlocks) {
        this.availableUnlocks = availableUnlocks;
    }

    public UnlockedItemPacket unlockedItemLimit(Map<Integer, Integer> unlockedItemLimit) {
        setUnlockedItemLimit(unlockedItemLimit);
        return this;
    }

    public UnlockedItemPacket availableUnlocks(boolean blinking) {
        setAvailableUnlocks(blinking);
        return this;
    }
}
