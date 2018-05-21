package com.btxtech.shared.gameengine.planet.gui.scenarioplayback;

import com.btxtech.shared.TestHelper;
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
    private boolean equals;

    public SyncItemProperty(String propertyName, String propertyActualValue, String propertyExpectedValue, boolean equals) {
        this.propertyName = propertyName;
        this.propertyActualValue = propertyActualValue;
        this.propertyExpectedValue = propertyExpectedValue;
        this.equals = equals;
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

    public boolean isEquals() {
        return equals;
    }

    public static SyncItemProperty createInt(String name, SyncBaseItemInfo actual, SyncBaseItemInfo expected, Function<SyncBaseItemInfo, Integer> intReader) {
        int actualInt = intReader.apply(actual);
        boolean equals;
        if (expected != null) {
            equals = intReader.apply(expected) == actualInt;
        } else {
            equals = false;
        }
        return new SyncItemProperty(name, Integer.toString(actualInt), expected != null ? Integer.toString(intReader.apply(expected)) : null, equals);
    }

    public static SyncItemProperty createDouble(String name, SyncBaseItemInfo actual, SyncBaseItemInfo expected, Function<SyncBaseItemInfo, Double> doubleReader) {
        double actualDouble = doubleReader.apply(actual);
        boolean equals;
        if (expected != null) {
            equals = doubleReader.apply(expected) == actualDouble;
        } else {
            equals = false;
        }
        return new SyncItemProperty(name, Double.toString(actualDouble), expected != null ? Double.toString(doubleReader.apply(expected)) : null, equals);
    }

    public static SyncItemProperty createDecimalPosition(String name, SyncBaseItemInfo actual, SyncBaseItemInfo expected, Function<SyncBaseItemInfo, DecimalPosition> decimalPositionReader) {
        String expectedPropertyExpectedValue = null;
        boolean equals = false;
        if (expected != null) {
            expectedPropertyExpectedValue = String.format("%.6f:%.6f", decimalPositionReader.apply(expected).getX(), decimalPositionReader.apply(expected).getY());
            equals = decimalPositionReader.apply(actual).equalsDelta(decimalPositionReader.apply(expected), TestHelper.DECIMAL_POTION_DELTA);
        }
        return new SyncItemProperty(name, String.format("%.6f:%.6f", decimalPositionReader.apply(actual).getX(), decimalPositionReader.apply(actual).getY()), expectedPropertyExpectedValue, equals);
    }

    public static SyncItemProperty createDecimalPositionList(String name, SyncBaseItemInfo actual, SyncBaseItemInfo expected, Function<SyncBaseItemInfo, List<DecimalPosition>> listReader) {
        StringBuilder actualString = new StringBuilder();
        listReader.apply(actual).forEach(decimalPosition -> actualString.append(String.format("%.2f:%.2f|", decimalPosition.getX(), decimalPosition.getY())));
        StringBuilder expectedString = new StringBuilder();
        if (expected != null) {
            listReader.apply(expected).forEach(decimalPosition -> expectedString.append(String.format("%.2f:%.2f|", decimalPosition.getX(), decimalPosition.getY())));
        }
        return new SyncItemProperty(name, actualString.toString(), expectedString.toString(), true);
    }

    public static SyncItemProperty createRad2Grad(String name, SyncBaseItemInfo actual, SyncBaseItemInfo expected, Function<SyncBaseItemInfo, Double> doubleReader) {
        boolean equals = false;
        if(expected != null) {
            equals = doubleReader.apply(actual).equals(doubleReader.apply(expected));
        }
        return new SyncItemProperty(name, String.format("%.3f°", Math.toDegrees(doubleReader.apply(actual))),
                expected != null ? String.format("%.3f°", Math.toDegrees(doubleReader.apply(expected))) : null, equals);
    }
}
