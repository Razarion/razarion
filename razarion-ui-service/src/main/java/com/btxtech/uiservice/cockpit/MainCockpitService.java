package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.uiservice.control.GameUiControl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class MainCockpitService {
    private final LevelService levelService;
    private MainCockpit mainCockpit;
    private Function<Integer, Rectangle> inventoryPositionProvider;
    /**
     * The level last put on screen, so a level-up can be told from the first update of a session.
     * Null until then: a player who returns at level six has not just reached it, and pointing at
     * the tech tree every time he loads the page would be noise rather than news.
     */
    private Integer shownLevelNumber;

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
        if (shownLevelNumber != null && levelConfig.getNumber() > shownLevelNumber
                && allowsMoreThanBefore(levelConfig)) {
            mainCockpit.techTreeHasNews();
        }
        shownLevelNumber = levelConfig.getNumber();
    }

    /**
     * Whether this level allows anything its predecessor did not - a type that was not buildable, or
     * more of one than before.
     * <p>
     * The condition is this and not "a level was reached", because on planet 117 only six of
     * seventeen levels allow anything new: 2 adds the viper, 3 the powerplant and the radar, 4
     * raises vipers from three to six, 6 adds the dockyard, 7 the hydra, 8 the transporter, and
     * from 9 on nothing changes at all. A prompt that points at nothing eleven times is ignored the
     * twelfth, which is the one that matters - level 6 is where the dockyard appears, and the quest
     * that asks for it is the one 66% of the players who reach it never finish.
     */
    private boolean allowsMoreThanBefore(LevelConfig level) {
        Map<Integer, Integer> allowed = level.getItemTypeLimitation();
        if (allowed == null) {
            return false;
        }
        LevelConfig previous = levelBefore(level);
        if (previous == null) {
            // The first level allows what it allows; there is nothing it is more than.
            return false;
        }
        Map<Integer, Integer> before = previous.getItemTypeLimitation();
        for (Map.Entry<Integer, Integer> entry : allowed.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            Integer had = before == null ? null : before.get(entry.getKey());
            if (had == null || entry.getValue() > had) {
                return true;
            }
        }
        return false;
    }

    /** The level one number below this one, or null when there is none. */
    private LevelConfig levelBefore(LevelConfig level) {
        LevelConfig found = null;
        for (LevelConfig candidate : levelService.getOrderedLevels()) {
            if (candidate.getNumber() == level.getNumber() - 1) {
                found = candidate;
            }
        }
        return found;
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
