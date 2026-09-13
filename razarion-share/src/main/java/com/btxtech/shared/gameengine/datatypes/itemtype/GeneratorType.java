package com.btxtech.shared.gameengine.datatypes.itemtype;

import org.teavm.flavour.json.JsonPersistable;

/**
 * User: Razarion contributors
 * Date: 23.12.2009
 * Time: 12:44:52
 */
@JsonPersistable
public class GeneratorType {
    private int wattage;

    public int getWattage() {
        return wattage;
    }

    public GeneratorType setWattage(int wattage) {
        this.wattage = wattage;
        return this;
    }
}
