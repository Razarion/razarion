package com.btxtech.server.service.engine;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class ServerInventoryService {
    @Inject
    private UserService userService;
    @Inject
    private InventoryItemCrudPersistence inventoryPersistence;
    @Inject
    private BaseItemService baseItemService;

    public void onBoxPicked(int userId, BoxContent boxContent) {
        throw new UnsupportedOperationException("... TODO ...");
//        if (boxContent.getCrystals() > 0) {
//            userService.persistAddCrystals(userId, boxContent.getCrystals());
//        }
//        boxContent.getInventoryItems().forEach(inventoryItem -> userService.persistAddInventoryItem(userId, inventoryPersistence.getEntity(inventoryItem.getId())));
        // TODO historyPersistence.onBoxPicked(userId, boxContent);
    }

    public InventoryInfo loadInventory(PlayerSession playerSession) {
        return userService.readInventoryInfo(playerSession.getUserContext().getUserId());
    }

    public int loadCrystals(PlayerSession playerSession) {
        return userService.readCrystals(playerSession.getUserContext().getUserId());
    }

    public void useInventoryItem(UseInventoryItem useInventoryItem, PlayerSession playerSession, PlayerBaseFull playerBaseFull) {
        throw new UnsupportedOperationException("... TODO ...");
//        InventoryInfo inventoryInfo = loadInventory(playerSession);
//        if (inventoryInfo.getInventoryItemIds() == null || !inventoryInfo.getInventoryItemIds().contains(useInventoryItem.getInventoryId())) {
//            throw new IllegalArgumentException("User does not have inventory. Inventory Id: " + useInventoryItem.getInventoryId() + ". HumanPlayerId: " + playerSession.getUserContext().getUserId());
//        }
//        userService.persistRemoveInventoryItem(playerSession.getUserContext().getUserId(), inventoryPersistence.getEntity(useInventoryItem.getInventoryId()));
//        baseItemService.useInventoryItem(useInventoryItem, playerBaseFull);
        // TODO historyPersistence.onInventoryItemUsed(playerSession.getUserContext().getUserId(), useInventoryItem.getInventoryId());
    }
}
