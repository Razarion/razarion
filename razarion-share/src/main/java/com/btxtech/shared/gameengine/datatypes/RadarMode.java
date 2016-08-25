package com.btxtech.shared.gameengine.datatypes;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Arrays;
import java.util.List;

/**
 * User: beat
 * Date: 12.02.2012
 * Time: 11:53:32
 */
@Portable
public enum RadarMode {
    DISABLED(0),
    NONE(1),
    MAP(2),
    MAP_AND_UNITS(3);
    private int value;

    RadarMode(int value) {
        this.value = value;
    }

    /**
     * @param mode given mode
     * @return true if the given mode is the same mode or a higher mode
     */
    public boolean sameOrHigher(RadarMode mode) {
        return mode.value >= value;
    }

    public static RadarMode getHigher(RadarMode radarMode1, RadarMode radarMode2) {
        if (radarMode1.value > radarMode2.value) {
            return radarMode1;
        } else if (radarMode1.value < radarMode2.value) {
            return radarMode2;
        } else {
            return radarMode1;
        }
    }

    public static List<RadarMode> getList() {
        return Arrays.asList(values());
    }
}
