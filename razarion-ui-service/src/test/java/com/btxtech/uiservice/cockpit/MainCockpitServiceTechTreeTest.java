package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.uiservice.control.GameUiControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * When the tech tree is worth pointing at.
 *
 * <p>Only six of the seventeen levels on planet 117 allow anything the one before did not: 2 adds
 * the viper, 3 the powerplant and the radar, 4 raises vipers from three to six, 6 the dockyard, 7
 * the hydra, 8 the transporter, and from nine on nothing changes at all. A mark on every level-up
 * would therefore point at nothing eleven times out of seventeen - and be ignored on the twelfth,
 * which is the one that matters: level 6 is where the dockyard appears, and the quest that asks for
 * it is the one two thirds of the players who reach it never finish.
 */
public class MainCockpitServiceTechTreeTest {
    private static final int VIPER = 3;
    private static final int DOCKYARD = 11;

    private MainCockpitService service;
    private CountingCockpit cockpit;

    /** Counts the calls that matter and ignores the rest of the interface. */
    private static class CountingCockpit implements MainCockpit {
        private int news;

        @Override
        public void techTreeHasNews() {
            news++;
        }

        @Override public void show() { }
        @Override public void hide() { }
        @Override public void displayResources(int resources) { }
        @Override public void displayXps(int xp, int xp2LevelUp) { }
        @Override public void displayLevel(int levelNumber) { }
        @Override public void displayItemCount(int itemCount, int usedHouseSpace, int houseSpace) { }
        @Override public void displayEnergy(int consuming, int generating) { }
        @Override public void showRadar(GameUiControl.RadarState radarState) { }
        @Override public void blinkAvailableUnlock(boolean show) { }
        @Override public void clean() { }
    }

    /**
     * Levels 1 to 4, shaped like the real ones: a new type at 2, nothing at 3, more of an existing
     * type at 4.
     */
    @Before
    public void setUp() {
        List<LevelConfig> levels = new ArrayList<>();
        levels.add(level(101, 1, Map.of()));
        levels.add(level(102, 2, Map.of(VIPER, 3)));
        levels.add(level(103, 3, Map.of(VIPER, 3)));
        levels.add(level(104, 4, Map.of(VIPER, 6)));
        levels.add(level(105, 5, Map.of(VIPER, 6, DOCKYARD, 1)));

        InitializeService initializeService = new InitializeService();
        LevelService levelService = new LevelService(initializeService);
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setLevelConfigs(levels);
        levelService.init(staticGameConfig);

        cockpit = new CountingCockpit();
        service = new MainCockpitService(levelService);
        service.init(cockpit);
    }

    private static LevelConfig level(int id, int number, Map<Integer, Integer> limitation) {
        LevelConfig levelConfig = new LevelConfig();
        levelConfig.setId(id);
        levelConfig.setNumber(number);
        levelConfig.setItemTypeLimitation(new HashMap<>(limitation));
        return levelConfig;
    }

    private void atLevel(int levelId) {
        service.updateLevelAndXp(new UserContext().levelId(levelId));
    }

    /**
     * Arriving at a level is not reaching it. A player who comes back at level four has not just
     * unlocked anything, and marking the tech tree on every page load is noise, not news.
     */
    @Test
    public void saysNothingOnTheFirstUpdateOfASession() {
        atLevel(104);

        Assert.assertEquals(0, cockpit.news);
    }

    @Test
    public void saysSoWhenALevelAddsAType() {
        atLevel(101);
        atLevel(102);

        Assert.assertEquals(1, cockpit.news);
    }

    /** Level 3 allows exactly what level 2 allowed. Pointing at the tech tree there is a lie. */
    @Test
    public void staysQuietWhenNothingChanges() {
        atLevel(102);
        atLevel(103);

        Assert.assertEquals(0, cockpit.news);
    }

    /**
     * More of a type the player already had is news too - the tech tree is a matrix of counts, and
     * three vipers becoming six is the whole content of level four.
     */
    @Test
    public void saysSoWhenALevelRaisesACount() {
        atLevel(103);
        atLevel(104);

        Assert.assertEquals(1, cockpit.news);
    }

    /** Two level-ups in a row, one of them empty: only the one that allows something speaks. */
    @Test
    public void speaksOncePerLevelThatAllowsSomething() {
        atLevel(101);
        atLevel(102);
        atLevel(103);
        atLevel(104);
        atLevel(105);

        Assert.assertEquals(3, cockpit.news);
    }

    /**
     * Losing a level is not a level-up. Nothing in the game does this today, but the condition is
     * "higher than what was shown", and a condition that only reads one way by accident is one that
     * breaks when something else changes.
     */
    @Test
    public void staysQuietGoingBackwards() {
        atLevel(105);
        atLevel(102);

        Assert.assertEquals(0, cockpit.news);
    }
}
