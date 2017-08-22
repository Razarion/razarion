package com.btxtech.shared.gameengine.planet.energy;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.BaseItemServiceBase;
import com.btxtech.shared.gameengine.planet.TestGameLogicListener;
import com.btxtech.shared.gameengine.planet.WeldBaseTest;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 21.08.2017.
 */
public class EnergyServiceTest extends WeldBaseTest {

    @Test
    public void test() {
        setupEnvironment();

        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(50, 50));
        tickBaseService();

        SyncBaseItem builder = CollectionUtils.getFirst(playerBaseFull.getItems());
        assertEnergy(0, 0, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());

        getCommandService().build(builder, new DecimalPosition(70, 70), getBaseItemType(BaseItemServiceBase.CONSUMER_ITEM_TYPE_ID));
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickBaseService(100);
        assertEnergy(0, 0, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickBaseService();
        assertEnergy(60, 0, playerBaseFull);
        SyncBaseItem consumer1 = findSyncBaseItem(playerBaseFull, BaseItemServiceBase.CONSUMER_ITEM_TYPE_ID);
        Assert.assertFalse(consumer1.getSyncConsumer().isOperating());
        assertGameLogicListener(60, 0, playerBaseFull);
        getTestGameLogicListener().clearAll();

        getCommandService().build(builder, new DecimalPosition(70, 100), getBaseItemType(BaseItemServiceBase.GENERATOR_ITEM_TYPE_ID));
        assertEnergy(60, 0, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickBaseService(100);
        assertEnergy(60, 0, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickBaseService();
        assertEnergy(60, 80, playerBaseFull);
        Assert.assertTrue(consumer1.getSyncConsumer().isOperating());
        SyncBaseItem generator1 = findSyncBaseItem(playerBaseFull, BaseItemServiceBase.GENERATOR_ITEM_TYPE_ID);
        assertGameLogicListener(60, 80, playerBaseFull);
        getTestGameLogicListener().clearAll();

        getCommandService().build(builder, new DecimalPosition(70, 130), getBaseItemType(BaseItemServiceBase.CONSUMER_ITEM_TYPE_ID));
        assertEnergy(60, 80, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickBaseService();
        assertEnergy(120, 80, playerBaseFull);
        SyncBaseItem consumer2 = findSyncBaseItem(playerBaseFull, BaseItemServiceBase.CONSUMER_ITEM_TYPE_ID, consumer1);
        Assert.assertFalse(consumer1.getSyncConsumer().isOperating());
        Assert.assertFalse(consumer2.getSyncConsumer().isOperating());
        assertGameLogicListener(120, 80, playerBaseFull);
        getTestGameLogicListener().clearAll();

        getCommandService().build(builder, new DecimalPosition(70, 160), getBaseItemType(BaseItemServiceBase.GENERATOR_ITEM_TYPE_ID));
        assertEnergy(120, 80, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickBaseService();
        assertEnergy(120, 160, playerBaseFull);
        Assert.assertTrue(consumer1.getSyncConsumer().isOperating());
        Assert.assertTrue(consumer2.getSyncConsumer().isOperating());
        SyncBaseItem generator2 = findSyncBaseItem(playerBaseFull, BaseItemServiceBase.GENERATOR_ITEM_TYPE_ID, generator1);
        assertGameLogicListener(120, 160, playerBaseFull);
        getTestGameLogicListener().clearAll();

        getCommandService().build(builder, new DecimalPosition(70, 190), getBaseItemType(BaseItemServiceBase.FACTORY_ITEM_TYPE_ID));
        tickBaseService();
        assertEnergy(120, 160, playerBaseFull);

        removeSyncItem(generator1);
        assertEnergy(120, 80, playerBaseFull);
        Assert.assertFalse(consumer1.getSyncConsumer().isOperating());
        Assert.assertFalse(consumer2.getSyncConsumer().isOperating());
        assertGameLogicListener(120, 80, playerBaseFull);
        getTestGameLogicListener().clearAll();

        removeSyncItem(consumer1);
        assertEnergy(60, 80, playerBaseFull);
        Assert.assertTrue(consumer2.getSyncConsumer().isOperating());
        assertGameLogicListener(60, 80, playerBaseFull);
        getTestGameLogicListener().clearAll();

        removeSyncItem(generator2);
        assertEnergy(60, 0, playerBaseFull);
        Assert.assertFalse(consumer2.getSyncConsumer().isOperating());
        assertGameLogicListener(60, 0, playerBaseFull);
        getTestGameLogicListener().clearAll();

        removeSyncItem(consumer2);
        assertEnergy(0, 0, playerBaseFull);
        assertGameLogicListener(0, 0, playerBaseFull);
    }

    private void assertEnergy(int consumingExpected, int generatingExpected, PlayerBase playerBase) {
        BaseEnergy baseEnergy = getBaseEnergy(playerBase);
        Assert.assertEquals("Consuming", consumingExpected, baseEnergy.getConsuming());
        Assert.assertEquals("Generating", generatingExpected, baseEnergy.getGenerating());
    }

    private BaseEnergy getBaseEnergy(PlayerBase playerBase) {
        EnergyService energyService = getWeldBean(EnergyService.class);
        return (BaseEnergy) SimpleTestEnvironment.callPrivateMethod("getBaseEnergy", energyService, new Class[]{PlayerBase.class}, new Object[]{playerBase});
    }

    private void assertGameLogicListener(int consumingExpected, int generatingExpected, PlayerBase playerBaseExpected) {
        Assert.assertEquals(1, getTestGameLogicListener().getEnergyStateChangedEntries().size());
        TestGameLogicListener.EnergyStateChangedEntry energyStateChangedEntry = getTestGameLogicListener().getEnergyStateChangedEntries().get(0);
        Assert.assertEquals("Base", playerBaseExpected, energyStateChangedEntry.getBase());
        Assert.assertEquals("Consuming", consumingExpected, energyStateChangedEntry.getConsuming());
        Assert.assertEquals("Generating", generatingExpected, energyStateChangedEntry.getGenerating());
    }
}