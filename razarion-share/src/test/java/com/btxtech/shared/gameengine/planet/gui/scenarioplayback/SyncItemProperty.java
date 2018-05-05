package com.btxtech.shared.gameengine.planet.gui.scenarioplayback;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

import java.util.List;
import java.util.function.Function;

/**
 * Created by Beat
 * on 13.04.2018.
 */
public class SyncItemProperty {
    private String propertyName;
    private String propertyActualValue;
    private String propertyExpectedValue;

    public SyncItemProperty(String propertyName, String propertyActualValue, String propertyExpectedValue) {
        this.propertyName = propertyName;
        this.propertyActualValue = propertyActualValue;
        this.propertyExpectedValue = propertyExpectedValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyActualValue() {
        return propertyActualValue;
    }

    public String getPropertyExpectedValue() {
        return propertyExpectedValue;
    }

    public static SyncItemProperty createInt(String name, SyncBaseItemInfo actual, SyncBaseItemInfo expected, Function<SyncBaseItemInfo, Integer> intReader) {
        return new SyncItemProperty(name, Integer.toString(intReader.apply(actual)), expected != null ? Integer.toString(intReader.apply(expected)) : null);
    }

    public static SyncItemProperty createDouble(String name, SyncBaseItemInfo actual, SyncBaseItemInfo expected, Function<SyncBaseItemInfo, Double> doubleReader) {
        return new SyncItemProperty(name, Double.toString(doubleReader.apply(actual)), expected != null ? Double.toString(doubleReader.apply(expected)) : null);
    }

    public static SyncItemProperty createDecimalPosition(String name, SyncBaseItemInfo actual, SyncBaseItemInfo expected, Function<SyncBaseItemInfo, DecimalPosition> decimalPositionReader) {
        String expectedPropertyExpectedValue = null;
        if (expected != null) {
            expectedPropertyExpectedValue = String.format("%.6f:%.6f", decimalPositionReader.apply(expected).getX(), decimalPositionReader.apply(expected).getY());
        }
        return new SyncItemProperty(name, String.format("%.6f:%.6f", decimalPositionReader.apply(actual).getX(), decimalPositionReader.apply(actual).getY()), expectedPropertyExpectedValue);
    }

    public static SyncItemProperty createDecimalPositionList(String name, SyncBaseItemInfo actual, SyncBaseItemInfo expected, Function<SyncBaseItemInfo, List<DecimalPosition>> listReader) {
        StringBuilder actualString = new StringBuilder();
        listReader.apply(actual).forEach(decimalPosition -> actualString.append(String.format("%.2f:%.2f|", decimalPosition.getX(), decimalPosition.getY())));
        StringBuilder expectedString = new StringBuilder();
        if (expected != null) {
            listReader.apply(expected).forEach(decimalPosition -> expectedString.append(String.format("%.2f:%.2f|", decimalPosition.getX(), decimalPosition.getY())));
        }
        return new SyncItemProperty(name, actualString.toString(), expectedString.toString());
    }

    public static SyncItemProperty createRad2Grad(String name, SyncBaseItemInfo actual, SyncBaseItemInfo expected, Function<SyncBaseItemInfo, Double> doubleReader) {
        return new SyncItemProperty(name, String.format("%.3f°", Math.toDegrees(doubleReader.apply(actual))),
                expected != null ? String.format("%.3f°", Math.toDegrees(doubleReader.apply(expected))) : null);
    }
}
