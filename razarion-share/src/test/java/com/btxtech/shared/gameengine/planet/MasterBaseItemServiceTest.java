package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Beat
 * 16.04.2017.
 */
public class MasterBaseItemServiceTest extends BaseItemServiceBase {
    @Test
    public void test() {
        PlanetConfig planetConfig = new PlanetConfig();

        setup(planetConfig, GameEngineMode.MASTER, null, null);

        PlayerBaseFull base1 = getBaseItemService().createHumanBase(1000, 1, new HumanPlayerId().setPlayerId(105), "Unit test Base human");
        PlayerBaseFull base2 = getBaseItemService().createBotBase(new BotConfig().setName("Test Bot").setNpc(false));
        int builderId = getBaseItemService().spawnSyncBaseItem(getBaseItemType(GameTestContent.BUILDER_ITEM_TYPE_ID), new DecimalPosition(100, 200), Math.toRadians(80), base1, true).getId();
        int factoryId = getBaseItemService().spawnSyncBaseItem(getBaseItemType(GameTestContent.FACTORY_ITEM_TYPE_ID), new DecimalPosition(200, 200), Math.toRadians(100), base2, true).getId();
        int attackerId = getBaseItemService().spawnSyncBaseItem(getBaseItemType(GameTestContent.ATTACKER_ITEM_TYPE_ID), new DecimalPosition(300, 200), Math.toRadians(120), base2, false).getId();

        // Verify bases
        List<PlayerBaseInfo> playerBaseInfos = getBaseItemService().getPlayerBaseInfos();
        Assert.assertEquals(2, playerBaseInfos.size());
        // Human
        PlayerBaseInfo humanBase = getPlayerBaseInfo(base1.getBaseId(), playerBaseInfos);
        Assert.assertEquals("Unit test Base human", humanBase.getName());
        Assert.assertEquals(Character.HUMAN, humanBase.getCharacter());
        Assert.assertEquals(105, humanBase.getHumanPlayerId().getPlayerId());
        Assert.assertEquals(1000, humanBase.getResources(), 0.0001);
        // Bot
        PlayerBaseInfo botBase = getPlayerBaseInfo(base2.getBaseId(), playerBaseInfos);
        Assert.assertEquals("Test Bot", botBase.getName());
        Assert.assertEquals(Character.BOT, botBase.getCharacter());
        Assert.assertNull(botBase.getHumanPlayerId());

        List<SyncBaseItemInfo> syncBaseItemInfos = getBaseItemService().getSyncBaseItemInfos();
        Assert.assertEquals(3, syncBaseItemInfos.size());

        // Verify builder
        SyncBaseItemInfo builderSyncInfo = getSyncBaseItemInfo(builderId, syncBaseItemInfos);
        Assert.assertEquals(base1.getBaseId(), builderSyncInfo.getBaseId());
        Assert.assertEquals(40, builderSyncInfo.getHealth(), 0.001);
        Assert.assertEquals(Math.toRadians(80), builderSyncInfo.getSyncPhysicalAreaInfo().getAngle(), 0.001);
        Assert.assertEquals(new DecimalPosition(100, 200), builderSyncInfo.getSyncPhysicalAreaInfo().getPosition());
        Assert.assertEquals(1.0, builderSyncInfo.getBuildup(), 0.0001);
        Assert.assertNull(builderSyncInfo.getCurrentBuildup());
        Assert.assertEquals(1.0, builderSyncInfo.getSpawnProgress(), 0.0001);
        // Verify Factory
        SyncBaseItemInfo factorySyncInfo = getSyncBaseItemInfo(factoryId, syncBaseItemInfos);
        Assert.assertEquals(base2.getBaseId(), factorySyncInfo.getBaseId());
        Assert.assertEquals(30, factorySyncInfo.getHealth(), 0.001);
        Assert.assertEquals(Math.toRadians(100), factorySyncInfo.getSyncPhysicalAreaInfo().getAngle(), 0.001);
        Assert.assertEquals(new DecimalPosition(200, 200), factorySyncInfo.getSyncPhysicalAreaInfo().getPosition());
        Assert.assertEquals(1.0, factorySyncInfo.getSpawnProgress(), 0.0001);
        // Verify Attacker
        SyncBaseItemInfo attackerSyncInfo = getSyncBaseItemInfo(attackerId, syncBaseItemInfos);
        Assert.assertEquals(base2.getBaseId(), attackerSyncInfo.getBaseId());
        Assert.assertEquals(20, attackerSyncInfo.getHealth(), 0.001);
        Assert.assertEquals(Math.toRadians(120), attackerSyncInfo.getSyncPhysicalAreaInfo().getAngle(), 0.001);
        Assert.assertEquals(new DecimalPosition(300, 200), attackerSyncInfo.getSyncPhysicalAreaInfo().getPosition());
        Assert.assertEquals(0.0, attackerSyncInfo.getSpawnProgress(), 0.0001);
    }

    private PlayerBaseInfo getPlayerBaseInfo(int baseId, List<PlayerBaseInfo> playerBaseInfos) {
        for (PlayerBaseInfo playerBaseInfo : playerBaseInfos) {
            if (playerBaseInfo.getBaseId() == baseId) {
                return playerBaseInfo;
            }
        }
        throw new IllegalArgumentException("No PlayerBaseInfo for id: " + baseId);
    }

    private SyncBaseItemInfo getSyncBaseItemInfo(int id, List<SyncBaseItemInfo> syncBaseItemInfos) {
        for (SyncBaseItemInfo syncBaseItemInfo : syncBaseItemInfos) {
            if (syncBaseItemInfo.getId() == id) {
                return syncBaseItemInfo;
            }
        }
        throw new IllegalArgumentException("No SyncBaseItemInfo for id: " + id);
    }
}
