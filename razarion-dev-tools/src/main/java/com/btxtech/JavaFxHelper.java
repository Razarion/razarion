package com.btxtech;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Created by Beat
 * 14.02.2017.
 */
public class JavaFxHelper {

    public static Paint toFxColor(com.btxtech.shared.datatypes.Color healthBar) {
        return new Color(healthBar.getR(), healthBar.getG(), healthBar.getB(), healthBar.getA());
    }
}
