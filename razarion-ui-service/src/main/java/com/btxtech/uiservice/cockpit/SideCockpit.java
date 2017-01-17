package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.Rectangle;

/**
 * Created by Beat
 * 16.11.2016.
 */
public interface SideCockpit {
    void show();

    void displayResources(int resources);

    void displayXps(int xp);

    void displayLevel(int levelNumber);

    Rectangle getInventoryDialogButtonLocation();

    void displayItemCount(int itemCount);
}
