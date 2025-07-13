package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.uiservice.control.GameUiControl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class MainCockpitService {
    private final LevelService levelService;
    private MainCockpit mainCockpit;
    private Function<Integer, Rectangle> inventoryPositionProvider;

    @Inject
    public MainCockpitService(LevelService levelService) {
        this.levelService = levelService;
    }

    public void init(MainCockpit sideCockpit) {
        this.mainCockpit = sideCockpit;
    }

    public void show(UserContext userContext) {
        mainCockpit.show();
        updateLevelAndXp(userContext);
    }

    public void hide() {
        mainCockpit.hide();
    }

    public void updateLevelAndXp(UserContext userContext) {
        if (userContext.getLevelId() == null) {
            return;
        }
        LevelConfig levelConfig = levelService.getLevel(userContext.getLevelId());
        mainCockpit.displayXps(userContext.getXp(), levelConfig.getXp2LevelUp());
        mainCockpit.displayLevel(levelConfig.getNumber());
    }

    public void updateResource(int resource) {
        mainCockpit.displayResources(resource);
    }

    public void onItemCountChanged(int itemCount, int usedHouseSpace, int houseSpace) {
        mainCockpit.displayItemCount(itemCount, usedHouseSpace, houseSpace);
    }

    public void onEnergyChanged(int consuming, int generating) {
        mainCockpit.displayEnergy(consuming, generating);
    }

    public void showRadar(GameUiControl.RadarState radarState) {
        mainCockpit.showRadar(radarState);
    }

    public void blinkAvailableUnlock(boolean show) {
        mainCockpit.blinkAvailableUnlock(show);
    }

}
