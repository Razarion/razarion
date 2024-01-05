package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.inventory.InventoryItemCrudPersistence;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.BaseItemService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 17.09.2017.
 */
@Singleton
public class ServerInventoryService {
    @Inject
    private UserService userService;
    @Inject
    private InventoryItemCrudPersistence inventoryPersistence;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private HistoryPersistence historyPersistence;

    public void onBoxPicked(int userId, BoxContent boxContent) {
        if (boxContent.getCrystals() > 0) {
            userService.persistAddCrystals(userId, boxContent.getCrystals());
        }
        boxContent.getInventoryItems().forEach(inventoryItem -> userService.persistAddInventoryItem(userId, inventoryPersistence.getEntity(inventoryItem.getId())));
        historyPersistence.onBoxPicked(userId, boxContent);
    }

    public InventoryInfo loadInventory(PlayerSession playerSession) {
        return userService.readInventoryInfo(playerSession.getUserContext().getUserId());
    }

    public int loadCrystals(PlayerSession playerSession) {
        return userService.readCrystals(playerSession.getUserContext().getUserId());
    }

    public void useInventoryItem(UseInventoryItem useInventoryItem, PlayerSession playerSession, PlayerBaseFull playerBaseFull) {
        InventoryInfo inventoryInfo = loadInventory(playerSession);
        if (inventoryInfo.getInventoryItemIds() == null || !inventoryInfo.getInventoryItemIds().contains(useInventoryItem.getInventoryId())) {
            throw new IllegalArgumentException("User does not have inventory. Inventory Id: " + useInventoryItem.getInventoryId() + ". HumanPlayerId: " + playerSession.getUserContext().getUserId());
        }
        userService.persistRemoveInventoryItem(playerSession.getUserContext().getUserId(), inventoryPersistence.getEntity(useInventoryItem.getInventoryId()));
        baseItemService.useInventoryItem(useInventoryItem, playerBaseFull);
        historyPersistence.onInventoryItemUsed(playerSession.getUserContext().getUserId(), useInventoryItem.getInventoryId());
    }
}
