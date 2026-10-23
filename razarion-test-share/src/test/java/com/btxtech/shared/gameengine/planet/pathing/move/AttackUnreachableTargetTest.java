package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.DaggerTerrainServiceTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An attack on a target the unit cannot reach becomes a move to the closest point, not an attack
 * that searches again and again. A search that does not find its target walks every reachable node,
 * and the unit planned anew each time it arrived at the end of the short way: 1.5 s on the
 * game-engine thread every few seconds, every player standing still (PROD 23.10.2026, thread dumps
 * all in AStar from SyncWeapon.tickAttack).
 * <p>
 * The test terrain has land around 220/220 that A* cannot reach from 80/160.
 */
public class AttackUnreachableTargetTest extends DaggerTerrainServiceTestBase {
    private static final DecimalPosition UNREACHABLE = new DecimalPosition(220, 220);

    @Before
    public void before() {
        setupTerrainTypeService(null, null, null, null, null, null);
    }

    @Test
    public void attackOnAnUnreachableTargetEnds() {
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.HARVESTER_ITEM_TYPE_ID).count(1).createDirectly(true)
                .place(new PlaceConfig().position(UNREACHABLE)).noRebuild(true));
        List<BotEnragementStateConfig> states = new ArrayList<>();
        states.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        List<BotConfig> botConfigs = new ArrayList<>();
        botConfigs.add(new BotConfig().id(1).actionDelay(1).botEnragementStateConfigs(states).name("Unreachable").npc(false));
        getBotService().startBots(botConfigs);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem target = findSyncBaseItem((PlayerBaseFull) getBotBase(1), FallbackConfig.HARVESTER_ITEM_TYPE_ID);

        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase = getBaseItemService().createHumanBase(0, userContext.getLevelId(), Collections.emptyMap(), userContext.getUserId(), userContext.getName());
        SyncBaseItem attacker = getBaseItemService().spawnSyncBaseItem(getItemTypeService().getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID),
                new DecimalPosition(80, 160), 0, playerBase, true);
        tickPlanetServiceBaseServiceActive();

        getCommandService().attack(attacker, target, true);
        // The command is carried out with the next tick
        tickPlanetService(1);

        Assert.assertFalse("an attack that can never fire", attacker.getSyncWeapon().isActive());
        Assert.assertTrue("drives as near as it gets", attacker.getSyncPhysicalMovable().hasDestination());
    }
}
