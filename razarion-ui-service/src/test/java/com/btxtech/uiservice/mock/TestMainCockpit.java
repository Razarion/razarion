package com.btxtech.uiservice.mock;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.uiservice.cockpit.MainCockpit;
import com.btxtech.uiservice.control.GameUiControl;

public class TestMainCockpit implements MainCockpit {
    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void displayResources(int resources) {

    }

    @Override
    public void displayXps(int xp, int xp2LevelUp) {

    }

    @Override
    public void displayLevel(int levelNumber) {

    }

    @Override
    public Rectangle getInventoryDialogButtonLocation() {
        return null;
    }

    @Override
    public Rectangle getScrollHomeButtonLocation() {
        return null;
    }

    @Override
    public void displayItemCount(int itemCount, int usedHouseSpace, int houseSpace) {

    }

    @Override
    public void displayEnergy(int consuming, int generating) {

    }

    @Override
    public void showRadar(GameUiControl.RadarState radarState) {

    }

    @Override
    public void blinkAvailableUnlock(boolean show) {

    }

    @Override
    public void clean() {

    }
}
