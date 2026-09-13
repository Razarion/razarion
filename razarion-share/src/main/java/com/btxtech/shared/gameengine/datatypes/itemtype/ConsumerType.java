package com.btxtech.shared.gameengine.datatypes.itemtype;

import org.teavm.flavour.json.JsonPersistable;

/**
 * User: Razarion contributors
 * Date: 23.12.2009
 * Time: 12:54:48
 */
@JsonPersistable
public class ConsumerType {
    private int wattage;

    public int getWattage() {
        return wattage;
    }

    public ConsumerType setWattage(int wattage) {
        this.wattage = wattage;
        return this;
    }
}
