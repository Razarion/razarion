package com.btxtech.uiservice.cockpit;

import com.btxtech.uiservice.control.GameUiControl;
public interface MainCockpit {
    void show();

    void hide();

    void displayResources(int resources);

    void displayXps(int xp, int xp2LevelUp);

    void displayLevel(int levelNumber);

    void displayItemCount(int itemCount, int usedHouseSpace, int houseSpace);

    void displayEnergy(int consuming, int generating);

    void showRadar(GameUiControl.RadarState radarState);

    void blinkAvailableUnlock(boolean show);

    void clean();
}
