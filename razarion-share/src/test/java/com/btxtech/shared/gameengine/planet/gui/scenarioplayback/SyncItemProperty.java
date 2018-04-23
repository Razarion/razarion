package com.btxtech.shared.gameengine.planet.gui.scenarioplayback;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * on 13.04.2018.
 */
public class SyncItemProperty {
    private String propertyName;
    private String propertyValue;

    public SyncItemProperty(String propertyName, String propertyValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public static SyncItemProperty create(String name, int value) {
        return new SyncItemProperty(name, Integer.toString(value));
    }

    public static SyncItemProperty create(String name, DecimalPosition decimalPosition) {
        return new SyncItemProperty(name, String.format("%.6f:%.6f", decimalPosition.getX(), decimalPosition.getY()));
    }

    public static SyncItemProperty createRad2Grad(String name, double rad) {
        return new SyncItemProperty(name, String.format("%.3f°", Math.toRadians(rad)));
    }
}
