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

    /**
     * A level was reached that allows something the one before it did not - a unit type that was
     * not buildable, or more of one than before. Says nothing about which: the tech tree shows the
     * whole matrix and marks the player's column, so pointing at it is the whole message.
     */
    void techTreeHasNews();

    void clean();
}
