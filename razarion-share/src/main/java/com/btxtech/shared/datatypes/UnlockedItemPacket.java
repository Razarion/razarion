package com.btxtech.shared.datatypes;

import java.util.Map;

/**
 * Created by Beat
 * on 24.09.2017.
 */
public class UnlockedItemPacket {
    private Map<Integer, Integer> unlockedItemLimit;

    public Map<Integer, Integer> getUnlockedItemLimit() {
        return unlockedItemLimit;
    }

    public UnlockedItemPacket setUnlockedItemLimit(Map<Integer, Integer> unlockedItemLimit) {
        this.unlockedItemLimit = unlockedItemLimit;
        return this;
    }
}
