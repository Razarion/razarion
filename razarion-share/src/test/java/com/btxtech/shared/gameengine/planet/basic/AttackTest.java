package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 18.11.2017.
 */
public class AttackTest extends BaseBasicTest {
    @Test
    public void land() {
        setup();

        // Setup target land bot
        SyncBaseItem botItem = setupBot("Kenny", GameTestContent.HARVESTER_ITEM_TYPE_ID, new DecimalPosition(230, 90), 1);

        // Human base
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(104, 144), getBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.FACTORY_ITEM_TYPE_ID);
        getCommandService().fabricate(factory, getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem attacker = findSyncBaseItem(playerBaseFull, GameTestContent.ATTACKER_ITEM_TYPE_ID);

        assertSyncItemCount(4, 0, 0);

        // Attack
        getCommandService().attack(attacker, botItem, true);
        tickPlanetServiceBaseServiceActive();

        assertSyncItemCount(3, 0, 0);

        // showDisplay();
    }

    @Test
    public void waterLandAttack() {
        setup();

        // Setup attacker water bot
        setupBot("Kenny2", GameTestContent.SHIP_ATTACKER_ITEM_TYPE_ID, new DecimalPosition(140, 222), 2);

        // Human water target base
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(104, 144), getBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.FACTORY_ITEM_TYPE_ID);
        getCommandService().fabricate(factory, getBaseItemType(GameTestContent.HARVESTER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem target = findSyncBaseItem(playerBaseFull, GameTestContent.HARVESTER_ITEM_TYPE_ID);
        getCommandService().move(target, new DecimalPosition(133, 185));
        tickPlanetServiceBaseServiceActive();

        assertSyncItemCount(4, 0, 0);

        // Attack
        BotAttackCommandConfig botAttackCommandConfig = new BotAttackCommandConfig();
        botAttackCommandConfig.setBotAuxiliaryId(2);
        botAttackCommandConfig.setTargetItemTypeId(GameTestContent.HARVESTER_ITEM_TYPE_ID);
        botAttackCommandConfig.setTargetSelection(new PlaceConfig().setPosition(new DecimalPosition(133, 185)).setRadius(100.0));
        botAttackCommandConfig.setActorItemTypeId(GameTestContent.SHIP_ATTACKER_ITEM_TYPE_ID);
        getBotService().executeCommands(Collections.singletonList(botAttackCommandConfig));

        // showDisplay();

        tickPlanetServiceBaseServiceActive();
        assertSyncItemCount(3, 0, 0);

    }

    @Test
    public void landWaterAttack() {
        setup();

        // Setup target water bot
        SyncBaseItem botItem = setupBot("Kenny3", GameTestContent.SHIP_HARVESTER_ITEM_TYPE_ID, new DecimalPosition(145, 199), 2);

        // Human land base
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(104, 144), getBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem factory = findSyncBaseItem(playerBaseFull, GameTestContent.FACTORY_ITEM_TYPE_ID);
        getCommandService().fabricate(factory, getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem attacker = findSyncBaseItem(playerBaseFull, GameTestContent.ATTACKER_ITEM_TYPE_ID);

        assertSyncItemCount(4, 0, 0);

        // Attack
        getCommandService().attack(attacker, botItem, true);
        tickPlanetServiceBaseServiceActive();
        // tickPlanetService(10000);

        assertSyncItemCount(3, 0, 0);

        // showDisplay();
    }
}
