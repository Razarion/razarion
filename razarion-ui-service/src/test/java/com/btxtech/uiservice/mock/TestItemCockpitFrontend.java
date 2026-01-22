package com.btxtech.uiservice.mock;

import com.btxtech.uiservice.cockpit.item.ItemCockpitFrontend;
import com.btxtech.uiservice.cockpit.item.OtherItemCockpit;
import com.btxtech.uiservice.cockpit.item.OwnItemCockpit;
import com.btxtech.uiservice.cockpit.item.OwnMultipleIteCockpit;

import jakarta.inject.Singleton;

public class TestItemCockpitFrontend implements ItemCockpitFrontend {
    @Override
    public void displayOwnSingleType(int count, OwnItemCockpit ownItemCockpit) {
        System.out.println("TestItemCockpitFrontend.displayOwnSingleType(): " + count + " " + ownItemCockpit);
    }

    @Override
    public void displayOwnMultipleItemTypes(OwnMultipleIteCockpit[] ownMultipleIteCockpits) {
        System.out.println("TestItemCockpitFrontend.displayOwnMultipleItemTypes(): " + ownMultipleIteCockpits);
    }

    @Override
    public void displayOtherItemType(OtherItemCockpit otherItemCockpit) {
        System.out.println("TestItemCockpitFrontend.displayOtherItemType(): " + otherItemCockpit);
    }

    @Override
    public void dispose() {
        System.out.println("TestItemCockpitFrontend.dispose()");
    }
}
