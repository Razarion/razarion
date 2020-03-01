package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by Beat
 * on 21.11.2017.
 */
public class GuardingTest extends BaseBasicTest {

    @Test
    public void land() {
        setup();

        // Setup target land bot
        SyncBaseItem botItem1 = setupBot("Kenny", FallbackConfig.HARVESTER_ITEM_TYPE_ID, new DecimalPosition(230, 90), 1);

        // Human base
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(104, 144), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem attacker = findSyncBaseItem(playerBaseFull, FallbackConfig.ATTACKER_ITEM_TYPE_ID);
        getCommandService().move(attacker, new DecimalPosition(160, 144));
        tickPlanetServiceBaseServiceActive();

        assertSyncItemCount(4, 0, 0);

        // Attack bot to human viper range
        BotMoveCommandConfig botMoveCommandConfig = new BotMoveCommandConfig();
        botMoveCommandConfig.setBotAuxiliaryId(1);
        botMoveCommandConfig.setBaseItemTypeId(FallbackConfig.HARVESTER_ITEM_TYPE_ID);
        botMoveCommandConfig.setTargetPosition(new DecimalPosition(173, 144));
        getBotService().executeCommands(Collections.singletonList(botMoveCommandConfig));
        tickPlanetServiceBaseServiceActive();

        assertSyncItemCount(3, 0, 0);

        // showDisplay();
    }

}
