package com.btxtech.shared.gameengine.planet.energy;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.TestGameLogicListener;
import com.btxtech.shared.gameengine.planet.DaggerMasterBaseTest;
import com.btxtech.shared.gameengine.planet.DaggerSlaveEmulator;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 21.08.2017.
 */
public class EnergyServiceTest extends DaggerMasterBaseTest {

    @Test
    public void testMaster() {
        setupMasterEnvironment();

        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(50, 50), createLevel1UserContext());
        tickPlanetServiceBaseServiceActive();

        SyncBaseItem builder = CollectionUtils.getFirst(playerBaseFull.getItems());
        assertEnergy(0, 0, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());

        getCommandService().build(builder, new DecimalPosition(70, 70), getBaseItemType(FallbackConfig.CONSUMER_ITEM_TYPE_ID));
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickPlanetService(53); // Building should be started but not finished
        assertEnergy(0, 0, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickPlanetServiceBaseServiceActive();
        assertEnergy(60, 0, playerBaseFull);
        SyncBaseItem consumer1 = findSyncBaseItem(playerBaseFull, FallbackConfig.CONSUMER_ITEM_TYPE_ID);
        assertGameLogicListener(60, 0, playerBaseFull);
        getTestGameLogicListener().clearAll();

        getCommandService().build(builder, new DecimalPosition(70, 100), getBaseItemType(FallbackConfig.GENERATOR_ITEM_TYPE_ID));
        assertEnergy(60, 0, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickPlanetService(72); // Building should be started but not finished
        assertEnergy(60, 0, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickPlanetServiceBaseServiceActive();
        assertEnergy(60, 80, playerBaseFull);
        SyncBaseItem generator1 = findSyncBaseItem(playerBaseFull, FallbackConfig.GENERATOR_ITEM_TYPE_ID);
        assertGameLogicListener(60, 80, playerBaseFull);
        getTestGameLogicListener().clearAll();

        getCommandService().build(builder, new DecimalPosition(70, 130), getBaseItemType(FallbackConfig.CONSUMER_ITEM_TYPE_ID));
        assertEnergy(60, 80, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickPlanetServiceBaseServiceActive();
        assertEnergy(120, 80, playerBaseFull);
        SyncBaseItem consumer2 = findSyncBaseItem(playerBaseFull, FallbackConfig.CONSUMER_ITEM_TYPE_ID, consumer1);
        assertGameLogicListener(120, 80, playerBaseFull);
        getTestGameLogicListener().clearAll();

        getCommandService().build(builder, new DecimalPosition(70, 160), getBaseItemType(FallbackConfig.GENERATOR_ITEM_TYPE_ID));
        assertEnergy(120, 80, playerBaseFull);
        Assert.assertTrue(getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        tickPlanetServiceBaseServiceActive();
        assertEnergy(120, 160, playerBaseFull);
        SyncBaseItem generator2 = findSyncBaseItem(playerBaseFull, FallbackConfig.GENERATOR_ITEM_TYPE_ID, generator1);
        assertGameLogicListener(120, 160, playerBaseFull);
        getTestGameLogicListener().clearAll();

        getCommandService().build(builder, new DecimalPosition(70, 190), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        assertEnergy(120, 160, playerBaseFull);

        removeSyncItem(generator1);
        tickPlanetService(1);
        assertEnergy(120, 80, playerBaseFull);
        assertGameLogicListener(120, 80, playerBaseFull);
        getTestGameLogicListener().clearAll();

        removeSyncItem(consumer1);
        tickPlanetService(1);
        assertEnergy(60, 80, playerBaseFull);
        assertGameLogicListener(60, 80, playerBaseFull);
        getTestGameLogicListener().clearAll();

        removeSyncItem(generator2);
        tickPlanetService(1);
        assertEnergy(60, 0, playerBaseFull);
        assertGameLogicListener(60, 0, playerBaseFull);
        getTestGameLogicListener().clearAll();

        removeSyncItem(consumer2);
        tickPlanetService(1);
        assertEnergy(0, 0, playerBaseFull);
        assertGameLogicListener(0, 0, playerBaseFull);
    }

    @Test
    public void testSlave() {
        setupMasterEnvironment();

        UserContext userContext = createLevel1UserContext();
        // Create Master base
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(50, 50), userContext);
        tickPlanetServiceBaseServiceActive();
        // Connect permanent slave and verify slave
        DaggerSlaveEmulator permanentSalve = new DaggerSlaveEmulator();
        permanentSalve.connectToMaster(userContext, this);
        assertConnectedSlave(0, 0, permanentSalve, userContext);
        Assert.assertTrue(permanentSalve.getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        assertNewConnectedSlave(0, 0, userContext);
        // Create master consumer
        SyncBaseItem builder = CollectionUtils.getFirst(playerBaseFull.getItems());
        getCommandService().build(builder, new DecimalPosition(70, 70), getBaseItemType(FallbackConfig.CONSUMER_ITEM_TYPE_ID));
        tickPlanetService(53); // Building should be started but not finished
        assertConnectedSlave(0, 0, permanentSalve, userContext);
        Assert.assertTrue(permanentSalve.getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        assertNewConnectedSlave(0, 0, userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem consumer1 = findSyncBaseItem(playerBaseFull, FallbackConfig.CONSUMER_ITEM_TYPE_ID);
        assertConnectedSlave(60, 0, permanentSalve, userContext);
        assertGameLogicListener(60, 0, permanentSalve, userContext);
        assertNewConnectedSlave(60, 0, userContext);
        // Add generator master
        getCommandService().build(builder, new DecimalPosition(70, 100), getBaseItemType(FallbackConfig.GENERATOR_ITEM_TYPE_ID));
        tickPlanetService(73); // Building should be started but not finished
        assertConnectedSlave(60, 0, permanentSalve, userContext);
        Assert.assertTrue(permanentSalve.getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        assertNewConnectedSlave(60, 0, userContext);
        tickPlanetServiceBaseServiceActive();
        assertConnectedSlave(60, 80, permanentSalve, userContext);
        assertGameLogicListener(60, 80, permanentSalve, userContext);
        assertNewConnectedSlave(60, 80, userContext);
        SyncBaseItem generator1 = findSyncBaseItem(playerBaseFull, FallbackConfig.GENERATOR_ITEM_TYPE_ID);
        // Add second consumer master
        getCommandService().build(builder, new DecimalPosition(70, 130), getBaseItemType(FallbackConfig.CONSUMER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem consumer2 = findSyncBaseItem(playerBaseFull, FallbackConfig.CONSUMER_ITEM_TYPE_ID, consumer1);
        assertConnectedSlave(120, 80, permanentSalve, userContext);
        assertGameLogicListener(120, 80, permanentSalve, userContext);
        assertNewConnectedSlave(120, 80, userContext);
        // Add second generator master
        getCommandService().build(builder, new DecimalPosition(70, 160), getBaseItemType(FallbackConfig.GENERATOR_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem generator2 = findSyncBaseItem(playerBaseFull, FallbackConfig.GENERATOR_ITEM_TYPE_ID, generator1);
        assertConnectedSlave(120, 160, permanentSalve, userContext);
        assertGameLogicListener(120, 160, permanentSalve, userContext);
        assertNewConnectedSlave(120, 160, userContext);
        // Add non consuming or generating item
        getCommandService().build(builder, new DecimalPosition(70, 190), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        assertConnectedSlave(120, 160, permanentSalve, userContext);
        Assert.assertTrue(permanentSalve.getTestGameLogicListener().getEnergyStateChangedEntries().isEmpty());
        assertNewConnectedSlave(120, 160, userContext);
        // Remove generator
        removeSyncItem(generator1);
        assertConnectedSlave(120, 80, permanentSalve, userContext);
        assertGameLogicListener(120, 80, permanentSalve, userContext);
        assertNewConnectedSlave(120, 80, userContext);
        // Remove consumer
        removeSyncItem(consumer1);
        assertConnectedSlave(60, 80, permanentSalve, userContext);
        assertGameLogicListener(60, 80, permanentSalve, userContext);
        assertNewConnectedSlave(60, 80, userContext);
        // Remove generator
        removeSyncItem(generator2);
        assertConnectedSlave(60, 0, permanentSalve, userContext);
        assertGameLogicListener(60, 0, permanentSalve, userContext);
        assertNewConnectedSlave(60, 0, userContext);
        // Remove consumer
        removeSyncItem(consumer2);
        assertConnectedSlave(0, 0, permanentSalve, userContext);
        assertGameLogicListener(0, 0, permanentSalve, userContext);
        assertNewConnectedSlave(0, 0, userContext);
    }

    public static void assertEnergy(int consumingExpected, int generatingExpected, EnergyService energyService, PlayerBase playerBase) {
        BaseEnergy baseEnergy = getBaseEnergy(energyService, playerBase);
        Assert.assertEquals("Consuming", consumingExpected, baseEnergy.getConsuming());
        Assert.assertEquals("Generating", generatingExpected, baseEnergy.getGenerating());
    }

    private void assertEnergy(int consumingExpected, int generatingExpected, PlayerBase playerBase) {
        assertEnergy(consumingExpected, generatingExpected, getEnergyService(), playerBase);
    }

    public static BaseEnergy getBaseEnergy(EnergyService energyService, PlayerBase playerBase) {
        return (BaseEnergy) SimpleTestEnvironment.callPrivateMethod("getBaseEnergy", energyService, new Class[]{PlayerBase.class}, new Object[]{playerBase});
    }

    private void assertGameLogicListener(int consumingExpected, int generatingExpected, PlayerBase playerBaseExpected) {
        Assert.assertEquals(1, getTestGameLogicListener().getEnergyStateChangedEntries().size());
        TestGameLogicListener.EnergyStateChangedEntry energyStateChangedEntry = getTestGameLogicListener().getEnergyStateChangedEntries().get(0);
        Assert.assertEquals("Base", playerBaseExpected, energyStateChangedEntry.getBase());
        Assert.assertEquals("Consuming", consumingExpected, energyStateChangedEntry.getConsuming());
        Assert.assertEquals("Generating", generatingExpected, energyStateChangedEntry.getGenerating());
    }

    private void assertGameLogicListener(int consumingExpected, int generatingExpected, DaggerSlaveEmulator weldSlaveEmulator, UserContext userContext) {
        Assert.assertEquals(1, weldSlaveEmulator.getTestGameLogicListener().getEnergyStateChangedEntries().size());
        TestGameLogicListener.EnergyStateChangedEntry energyStateChangedEntry = weldSlaveEmulator.getTestGameLogicListener().getEnergyStateChangedEntries().get(0);
        Assert.assertEquals("Base", weldSlaveEmulator.getPlayerBase(userContext), energyStateChangedEntry.getBase());
        Assert.assertEquals("Consuming", consumingExpected, energyStateChangedEntry.getConsuming());
        Assert.assertEquals("Generating", generatingExpected, energyStateChangedEntry.getGenerating());
        weldSlaveEmulator.getTestGameLogicListener().clearAll();
    }

    private void assertNewConnectedSlave(int consumingExpected, int generatingExpected, UserContext userContext) {
        DaggerSlaveEmulator slaveNew = new DaggerSlaveEmulator();
        slaveNew.connectToMaster(userContext, this);
        slaveNew.tickPlanetService();
        assertEnergy(consumingExpected, generatingExpected, slaveNew.getEnergyService(), slaveNew.getPlayerBase(userContext));
        slaveNew.disconnectFromMaster();
    }

    private void assertConnectedSlave(int consumingExpected, int generatingExpected, DaggerSlaveEmulator permanentSalve, UserContext userContext) {
        permanentSalve.tickPlanetService();
        while (permanentSalve.hasPendingReceivedTickInfos()) {
            permanentSalve.tickPlanetService();
        }
        permanentSalve.tickPlanetService();
        assertEnergy(consumingExpected, generatingExpected, permanentSalve.getEnergyService(), permanentSalve.getPlayerBase(userContext));
    }

}