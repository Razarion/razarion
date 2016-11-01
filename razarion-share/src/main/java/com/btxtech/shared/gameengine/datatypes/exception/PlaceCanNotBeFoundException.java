package com.btxtech.shared.gameengine.datatypes.exception;


import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;

/**
 * User: beat
 * Date: 16.10.2011
 * Time: 15:25:58
 */
public class PlaceCanNotBeFoundException extends RuntimeException {
    public PlaceCanNotBeFoundException() {
    }

    public PlaceCanNotBeFoundException(ItemType itemType, Rectangle region, int itemFreeRange) {
        super("Can not find free position. itemType: " + itemType + " region: " + region + " itemFreeRange: " + itemFreeRange);
    }

    public PlaceCanNotBeFoundException(double radius, PlaceConfig placeConfig) {
        super("Can not find free position. radius: " + radius + " region Id: " + placeConfig);
    }
}
