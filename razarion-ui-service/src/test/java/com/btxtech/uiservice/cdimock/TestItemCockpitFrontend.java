package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.cockpit.item.ItemCockpitFrontend;
import com.btxtech.uiservice.cockpit.item.OtherItemCockpit;
import com.btxtech.uiservice.cockpit.item.OwnItemCockpit;
import com.btxtech.uiservice.cockpit.item.OwnMultipleIteCockpit;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestItemCockpitFrontend implements ItemCockpitFrontend {
    @Override
    public void displayOwnSingleType(int count, OwnItemCockpit ownItemCockpit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void displayOwnMultipleItemTypes(OwnMultipleIteCockpit[] ownMultipleIteCockpits) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void displayOtherItemType(OtherItemCockpit otherItemCockpit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException();
    }
}
