package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.gui.userobject.ScenarioPlayback;
import com.btxtech.shared.gameengine.planet.pathing.AStarBaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 12.04.2018.
 */
public class MoveBaseTest extends AStarBaseTest {
    public static final String SAVE_DIRECTORY = TestHelper.SAVE_DIRECTORY + "pathing//move";

    protected void testScenario(Scenario scenario) {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase1 = getBaseItemService().createHumanBase(0, userContext.getLevelId(), Collections.emptyMap(), userContext.getHumanPlayerId(), userContext.getName());
        playerBase1.setResources(Double.MAX_VALUE);

        scenario.setup(playerBase1, getItemTypeService(), getBaseItemService());
        scenario.createSyncItems();


        List<List<SyncBaseItemInfo>> ticks = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            ticks.add(getBaseItemService().getSyncBaseItemInfos());
            tickPlanetService();
        }
        ticks.add(getBaseItemService().getSyncBaseItemInfos());

        showDisplay(new ScenarioPlayback().setSyncBaseItemInfo(ticks));

        try {
            new ObjectMapper().writeValue(new File(SAVE_DIRECTORY, scenario.getFileName()), ticks);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
