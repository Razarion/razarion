package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.uiservice.control.GameUiControl;

/**
 * Created by Beat
 * 16.11.2016.
 */
public interface SideCockpit {
    void show();

    void hide();

    void displayResources(int resources);

    void displayXps(int xp, int xp2LevelUp);

    void displayLevel(int levelNumber);

    Rectangle getInventoryDialogButtonLocation();

    Rectangle getScrollHomeButtonLocation();

    void displayItemCount(int itemCount, int houseSpace);

    void displayEnergy(int consuming, int generating);

    void showRadar(GameUiControl.RadarState radarState);

    void clean();
}
