package com.btxtech.server.gameengine;

import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.TestHelper;
import com.btxtech.server.persistence.history.InventoryHistoryEntry;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;

/**
 * Created by Beat
 * on 18.09.2017.
 */
public class RestServerInventoryServiceTestBase extends IgnoreOldArquillianTest {
    @Inject
    private ServerInventoryService serverInventoryService;
    @Inject
    private InventoryTypeService inventoryTypeService;
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private ServerGameEngineControl serverGameEngineControl;
    @Inject
    private BaseItemService baseItemService;

    @Before
    public void before() throws Exception {
        setupPlanets();
        cleanTable(InventoryHistoryEntry.class);
    }

    @After
    public void after() throws Exception {
        cleanTable(UserEntity.class);
        cleanPlanets();
        cleanTable(InventoryHistoryEntry.class);
    }

    @Test
    public void testLoadInventoryRegistered() throws Exception {
        // Start from ServletContextMonitor.contextInitialized() not working
        serverGameEngineControl.start(null, true);
        UserContext userContext = handleFacebookUserLogin("0000001");
        int userId = userContext.getHumanPlayerId().getUserId();
        PlayerBaseFull playerBaseFull = baseItemService.createHumanBaseWithBaseItem(LEVEL_4_ID, Collections.emptyMap(), sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId(), "Test base", new DecimalPosition(100, 100));
        // Verify
        InventoryInfo inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(0, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryItemIds().isEmpty());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        assertUserEntity(userId, 0);
        // Pick 1. box
        BoxContent boxContent = new BoxContent();
        boxContent.addInventoryItem(inventoryTypeService.getInventoryItem(INVENTORY_ITEM_1_ID));
        boxContent.addCrystals(99);
        serverInventoryService.onBoxPicked(sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId(), boxContent);
        assertCount(2, InventoryHistoryEntry.class);
        // Verify
        inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(99, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        Assert.assertEquals(1, inventoryInfo.getInventoryItemIds().size());
        Assert.assertEquals(INVENTORY_ITEM_1_ID, (int) inventoryInfo.getInventoryItemIds().get(0));
        assertUserEntity(userId, 99, INVENTORY_ITEM_1_ID);
        // Pick 2. box
        boxContent = new BoxContent();
        boxContent.addInventoryItem(inventoryTypeService.getInventoryItem(INVENTORY_ITEM_1_ID));
        boxContent.addCrystals(11);
        serverInventoryService.onBoxPicked(sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId(), boxContent);
        assertCount(4, InventoryHistoryEntry.class);
        // Verify
        inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(110, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        Assert.assertEquals(2, inventoryInfo.getInventoryItemIds().size());
        Assert.assertEquals(INVENTORY_ITEM_1_ID, (int) inventoryInfo.getInventoryItemIds().get(0));
        Assert.assertEquals(INVENTORY_ITEM_1_ID, (int) inventoryInfo.getInventoryItemIds().get(1));
        assertUserEntity(userId, 110, INVENTORY_ITEM_1_ID, INVENTORY_ITEM_1_ID);
        // Use 1. inventory item
        UseInventoryItem useInventoryItem = new UseInventoryItem().setInventoryId(INVENTORY_ITEM_1_ID);
        serverInventoryService.useInventoryItem(useInventoryItem, sessionHolder.getPlayerSession(), playerBaseFull);
        // Verify
        inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(110, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        Assert.assertEquals(1, inventoryInfo.getInventoryItemIds().size());
        Assert.assertEquals(INVENTORY_ITEM_1_ID, (int) inventoryInfo.getInventoryItemIds().get(0));
        assertUserEntity(userId, 110, INVENTORY_ITEM_1_ID);
        assertCount(5, InventoryHistoryEntry.class);
        // Use 2. inventory item
        useInventoryItem = new UseInventoryItem().setInventoryId(INVENTORY_ITEM_1_ID);
        serverInventoryService.useInventoryItem(useInventoryItem, sessionHolder.getPlayerSession(), playerBaseFull);
        // Verify
        inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(110, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        Assert.assertTrue(inventoryInfo.getInventoryItemIds().isEmpty());
        assertUserEntity(userId, 110);
        assertCount(6, InventoryHistoryEntry.class);
        // Use 3. inventory item not available
        useInventoryItem = new UseInventoryItem().setInventoryId(INVENTORY_ITEM_1_ID);
        try {
            serverInventoryService.useInventoryItem(useInventoryItem, sessionHolder.getPlayerSession(), playerBaseFull);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        // Verify
        inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(110, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        Assert.assertTrue(inventoryInfo.getInventoryItemIds().isEmpty());
        assertUserEntity(userId, 110);
        assertCount(6, InventoryHistoryEntry.class);
    }

    @Test
    public void testLoadInventoryUnregistered() throws Exception {
        // Start from ServletContextMonitor.contextInitialized() not working
        serverGameEngineControl.start(null, true);
        UserContext userContext = userService.getUserContextFromSession(); // Simulate anonymous login
        PlayerBaseFull playerBaseFull = baseItemService.createHumanBaseWithBaseItem(LEVEL_4_ID, Collections.emptyMap(), sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId(), "Test base", new DecimalPosition(100, 100));
        // Verify
        InventoryInfo inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(0, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryItemIds().isEmpty());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        assertUnregisteredUser(0);
        // Pick 1. box
        BoxContent boxContent = new BoxContent();
        boxContent.addInventoryItem(inventoryTypeService.getInventoryItem(INVENTORY_ITEM_1_ID));
        boxContent.addCrystals(99);
        serverInventoryService.onBoxPicked(sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId(), boxContent);
        assertCount(2, InventoryHistoryEntry.class);
        // Verify
        inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(99, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        Assert.assertEquals(1, inventoryInfo.getInventoryItemIds().size());
        Assert.assertEquals(INVENTORY_ITEM_1_ID, (int) inventoryInfo.getInventoryItemIds().get(0));
        assertUnregisteredUser(99, INVENTORY_ITEM_1_ID);
        // Pick 2. box
        boxContent = new BoxContent();
        boxContent.addInventoryItem(inventoryTypeService.getInventoryItem(INVENTORY_ITEM_1_ID));
        boxContent.addCrystals(11);
        serverInventoryService.onBoxPicked(sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId(), boxContent);
        assertCount(4, InventoryHistoryEntry.class);
        // Verify
        inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(110, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        Assert.assertEquals(2, inventoryInfo.getInventoryItemIds().size());
        Assert.assertEquals(INVENTORY_ITEM_1_ID, (int) inventoryInfo.getInventoryItemIds().get(0));
        Assert.assertEquals(INVENTORY_ITEM_1_ID, (int) inventoryInfo.getInventoryItemIds().get(1));
        assertUnregisteredUser(110, INVENTORY_ITEM_1_ID, INVENTORY_ITEM_1_ID);
        // Use 1. inventory item
        UseInventoryItem useInventoryItem = new UseInventoryItem().setInventoryId(INVENTORY_ITEM_1_ID);
        serverInventoryService.useInventoryItem(useInventoryItem, sessionHolder.getPlayerSession(), playerBaseFull);
        // Verify
        inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(110, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        Assert.assertEquals(1, inventoryInfo.getInventoryItemIds().size());
        Assert.assertEquals(INVENTORY_ITEM_1_ID, (int) inventoryInfo.getInventoryItemIds().get(0));
        assertUnregisteredUser(110, INVENTORY_ITEM_1_ID);
        assertCount(5, InventoryHistoryEntry.class);
        // Use 2. inventory item
        useInventoryItem = new UseInventoryItem().setInventoryId(INVENTORY_ITEM_1_ID);
        serverInventoryService.useInventoryItem(useInventoryItem, sessionHolder.getPlayerSession(), playerBaseFull);
        // Verify
        inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(110, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        Assert.assertTrue(inventoryInfo.getInventoryItemIds().isEmpty());
        assertUnregisteredUser(110);
        assertCount(6, InventoryHistoryEntry.class);
        // Use 3. inventory item not available
        useInventoryItem = new UseInventoryItem().setInventoryId(INVENTORY_ITEM_1_ID);
        try {
            serverInventoryService.useInventoryItem(useInventoryItem, sessionHolder.getPlayerSession(), playerBaseFull);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        // Verify
        inventoryInfo = serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        Assert.assertEquals(110, inventoryInfo.getCrystals());
        Assert.assertTrue(inventoryInfo.getInventoryArtifactIds().isEmpty());
        Assert.assertTrue(inventoryInfo.getInventoryItemIds().isEmpty());
        assertUnregisteredUser(110);
        assertCount(6, InventoryHistoryEntry.class);
    }

    private void assertUserEntity(int userId, int expectedCrystals, Integer... expectedInventoryItemIds) throws Exception {
        runInTransaction(entityManager -> {
            InventoryInfo inventoryInfo = entityManager.find(UserEntity.class, userId).toInventoryInfo();
            Assert.assertEquals(expectedCrystals, inventoryInfo.getCrystals());
            TestHelper.assertIds(inventoryInfo.getInventoryItemIds(), expectedInventoryItemIds);
        });
    }

    private void assertUnregisteredUser(int expectedCrystals, Integer... expectedInventoryItemIds) throws Exception {
        InventoryInfo inventoryInfo = sessionHolder.getPlayerSession().getUnregisteredUser().toInventoryInfo();
        Assert.assertEquals(expectedCrystals, inventoryInfo.getCrystals());
        TestHelper.assertIds(inventoryInfo.getInventoryItemIds(), expectedInventoryItemIds);
    }
}