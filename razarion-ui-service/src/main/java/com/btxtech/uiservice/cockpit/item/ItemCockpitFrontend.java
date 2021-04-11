package com.btxtech.uiservice.cockpit.item;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 30.09.2016.
 */
@JsType(isNative = true)
public interface ItemCockpitFrontend {
    void displayOwnSingleType(int count, OwnItemCockpit ownItemCockpit);

    void displayOwnMultipleItemTypes(OwnMultipleIteCockpit[] ownMultipleIteCockpits);

    void displayOtherItemType(OtherItemCockpit otherItemCockpit);

    void dispose();

    @Deprecated
    void maximizeMinButton();
}
