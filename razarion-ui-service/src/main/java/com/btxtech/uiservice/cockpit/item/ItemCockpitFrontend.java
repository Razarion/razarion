package com.btxtech.uiservice.cockpit.item;

/**
 * Created by Beat
 * 30.09.2016.
 */
public interface ItemCockpitFrontend {
    void displayOwnSingleType(int count, OwnItemCockpit ownItemCockpit);

    void displayOwnMultipleItemTypes(OwnMultipleIteCockpit[] ownMultipleIteCockpits);

    void displayOtherItemType(OtherItemCockpit otherItemCockpit);

    void dispose();
}
