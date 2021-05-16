package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncPhysicalAreaInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 16.04.2017.
 */
public class SlaveBaseItemServiceTest extends BaseItemServiceBase {
    @Test
    public void test() {
        InitialSlaveSyncItemInfo initialSlaveSyncItemInfo = new InitialSlaveSyncItemInfo();
        // Setup bases
        List<PlayerBaseInfo> playerBaseInfos = new ArrayList<>();
        playerBaseInfos.add(new PlayerBaseInfo().setBaseId(99).setCharacter(Character.HUMAN).setName("Test human base 1").setResources(211).setUserId(105));
        playerBaseInfos.add(new PlayerBaseInfo().setBaseId(40).setCharacter(Character.BOT).setName("Test bot base 1"));
        initialSlaveSyncItemInfo.setPlayerBaseInfos(playerBaseInfos);
        // Setup SyncBaseItemInfo
        List<SyncBaseItemInfo> syncBaseItemInfos = new ArrayList<>();
        // Attacker
        SyncPhysicalAreaInfo syncPhysicalAreaInfo = new SyncPhysicalAreaInfo().setPosition(new DecimalPosition(500, 200)).setAngle(0);
        SyncBaseItemInfo attackerInfo = new SyncBaseItemInfo().id(203).baseId(40).itemTypeId(FallbackConfig.ATTACKER_ITEM_TYPE_ID).syncPhysicalAreaInfo(syncPhysicalAreaInfo).buildup(1.0).health(0.99).spawnProgress(1.0);
        attackerInfo.target(15).followTarget(true).setReloadProgress(0.75);
        syncBaseItemInfos.add(attackerInfo);
        // Builder
        syncPhysicalAreaInfo = new SyncPhysicalAreaInfo().setPosition(new DecimalPosition(100, 200)).setAngle(Math.toRadians(45));
        SyncBaseItemInfo builderInfo = new SyncBaseItemInfo().id(15).baseId(99).itemTypeId(FallbackConfig.BUILDER_ITEM_TYPE_ID).syncPhysicalAreaInfo(syncPhysicalAreaInfo).buildup(1.0).health(0.34).spawnProgress(0.5);
        builderInfo.toBeBuiltTypeId(FallbackConfig.FACTORY_ITEM_TYPE_ID).setToBeBuildPosition(new DecimalPosition(200, 200));
        syncBaseItemInfos.add(builderInfo);
        // Factory
        syncPhysicalAreaInfo = new SyncPhysicalAreaInfo().setPosition(new DecimalPosition(300, 200)).setAngle(0);
        SyncBaseItemInfo factoryInfo = new SyncBaseItemInfo().id(107).baseId(99).itemTypeId(FallbackConfig.FACTORY_ITEM_TYPE_ID).syncPhysicalAreaInfo(syncPhysicalAreaInfo).buildup(1.0).health(0.5).spawnProgress(0.0);
        factoryInfo.factoryBuildupProgress(0.45).toBeBuiltTypeId(FallbackConfig.BUILDER_ITEM_TYPE_ID).rallyPoint(new DecimalPosition(300, 150));
        syncBaseItemInfos.add(factoryInfo);
        initialSlaveSyncItemInfo.setSyncBaseItemInfos(syncBaseItemInfos);

        setup(new PlanetConfig(), GameEngineMode.SLAVE, null, initialSlaveSyncItemInfo);

        // Verify
        SyncBaseItem builder = getSyncItemContainerService().getSyncBaseItemSave(15);
        // Verify Builder
        Assert.assertEquals(99, builder.getBase().getBaseId());
        Assert.assertEquals("Test human base 1", builder.getBase().getName());
        Assert.assertEquals(Character.HUMAN, builder.getBase().getCharacter());
        Assert.assertEquals(211, builder.getBase().getResources(), 0.0001);
        Assert.assertEquals(105, (int)builder.getBase().getUserId());
        Assert.assertEquals(1.0, builder.getBuildup(), 0.0001);
        Assert.assertEquals(0.34, builder.getHealth(), 0.0001);
        Assert.assertEquals(FallbackConfig.BUILDER_ITEM_TYPE_ID, builder.getBaseItemType().getId());
        Assert.assertEquals(new DecimalPosition(100, 200), builder.getSyncPhysicalMovable().getPosition2d());
        Assert.assertEquals(new Vertex(100, 200, -1.7), builder.getSyncPhysicalMovable().getPosition3d());
        Assert.assertEquals(2, builder.getSyncPhysicalMovable().getRadius(), 0.0001);
        Assert.assertEquals(Math.toRadians(45), builder.getSyncPhysicalMovable().getAngle(), 0.0001);
        Assert.assertNotNull(builder.getSyncBuilder());
        Assert.assertNull(builder.getSyncFactory());
        Assert.assertNull(builder.getSyncWeapon());
        Assert.assertEquals(new DecimalPosition(200, 200), SimpleTestEnvironment.readField("toBeBuildPosition", builder.getSyncBuilder()));
        Assert.assertNull(builder.getSyncBuilder().getCurrentBuildup());
        Assert.assertEquals(0.5, builder.getSpawnProgress(), 0.0001);
        // Verify Factory
        SyncBaseItem factory = getSyncItemContainerService().getSyncBaseItemSave(107);
        Assert.assertEquals(99, factory.getBase().getBaseId());
        Assert.assertEquals("Test human base 1", factory.getBase().getName());
        Assert.assertEquals(Character.HUMAN, factory.getBase().getCharacter());
        Assert.assertEquals(211, factory.getBase().getResources(), 0.0001);
        Assert.assertEquals(105, (int)factory.getBase().getUserId());
        Assert.assertEquals(1.0, factory.getBuildup(), 0.0001);
        Assert.assertEquals(0.5, factory.getHealth(), 0.0001);
        Assert.assertEquals(FallbackConfig.FACTORY_ITEM_TYPE_ID, factory.getBaseItemType().getId());
        Assert.assertEquals(new DecimalPosition(300, 200), factory.getSyncPhysicalMovable().getPosition2d());
        Assert.assertEquals(new Vertex(300, 200, -1.7), factory.getSyncPhysicalMovable().getPosition3d());
        Assert.assertEquals(5, factory.getSyncPhysicalMovable().getRadius(), 0.0001);
        Assert.assertEquals(0, factory.getSyncPhysicalMovable().getAngle(), 0.0001);
        Assert.assertNull(factory.getSyncBuilder());
        Assert.assertNull(factory.getSyncWeapon());
        Assert.assertNotNull(factory.getSyncFactory());
        Assert.assertEquals(0.45, factory.getSyncFactory().getBuildup(), 0.00001);
        Assert.assertEquals(FallbackConfig.BUILDER_ITEM_TYPE_ID, ((BaseItemType) SimpleTestEnvironment.readField("toBeBuiltType", factory.getSyncFactory())).getId());
        Assert.assertEquals(new DecimalPosition(300, 150), SimpleTestEnvironment.readField("rallyPoint", factory.getSyncFactory()));
        Assert.assertEquals(0.0, factory.getSpawnProgress(), 0.0001);
        // Attacker
        SyncBaseItem attacker = getSyncItemContainerService().getSyncBaseItemSave(203);
        Assert.assertEquals(40, attacker.getBase().getBaseId());
        Assert.assertEquals("Test bot base 1", attacker.getBase().getName());
        Assert.assertEquals(Character.BOT, attacker.getBase().getCharacter());
        Assert.assertEquals(1.0, attacker.getBuildup(), 0.0001);
        Assert.assertEquals(0.99, attacker.getHealth(), 0.0001);
        Assert.assertEquals(FallbackConfig.ATTACKER_ITEM_TYPE_ID, attacker.getBaseItemType().getId());
        Assert.assertEquals(new DecimalPosition(500, 200), attacker.getSyncPhysicalMovable().getPosition2d());
        Assert.assertEquals(new Vertex(500, 200, -1.7), attacker.getSyncPhysicalMovable().getPosition3d());
        Assert.assertEquals(3, attacker.getSyncPhysicalMovable().getRadius(), 0.0001);
        Assert.assertEquals(0, attacker.getSyncPhysicalMovable().getAngle(), 0.0001);
        Assert.assertNull(attacker.getSyncBuilder());
        Assert.assertNull(attacker.getSyncFactory());
        Assert.assertNotNull(attacker.getSyncWeapon());
        Assert.assertEquals(15, attacker.getSyncWeapon().getTarget().getId());
        Assert.assertEquals(0.75, (double) SimpleTestEnvironment.readField("reloadProgress", attacker.getSyncWeapon()), 0.0001);
        Assert.assertEquals(1.0, attacker.getSpawnProgress(), 0.0001);
    }

}
