package com.btxtech.server.service.engine;

import com.btxtech.server.service.history.HistoryService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;

@Service
public class ServerInventoryService {
    @Inject
    private UserService userService;
    @Inject
    private InventoryItemService inventoryPersistence;
    @Inject
    private InventoryArtifactService inventoryArtifactPersistence;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private HistoryService historyService;

    public void onBoxPicked(String userId, BoxContent boxContent) {
        if (boxContent.getCrystals() > 0) {
            userService.persistAddCrystals(userId, boxContent.getCrystals());
            historyService.onCrystalsFromBox(userId, boxContent.getCrystals());
        }
        boxContent.getInventoryItems().forEach(inventoryItem -> {
            userService.persistAddInventoryItem(userId, inventoryPersistence.getEntity(inventoryItem.getId()));
            historyService.onInventoryItemFromBox(userId, inventoryItem.getId());
        });
        boxContent.getInventoryArtifacts().forEach(inventoryArtifact -> {
            userService.persistAddInventoryArtifact(userId, inventoryArtifactPersistence.getEntity(inventoryArtifact.getId()));
            historyService.onInventoryArtifactFromBox(userId, inventoryArtifact.getId());
        });
        historyService.onBoxPicked(userId, boxContent);
    }

    public InventoryInfo loadInventory(String userId) {
        return userService.readInventoryInfo(userId);
    }

    public int loadCrystals(String userId) {
        return userService.readCrystals(userId);
    }

    /**
     * Place an owned inventory item on the map and consume it from the user's inventory.
     */
    @Transactional
    public void useInventoryItem(UseInventoryItem useInventoryItem, String userId) {
        InventoryInfo inventoryInfo = loadInventory(userId);
        if (inventoryInfo.getInventoryItemIds() == null || !inventoryInfo.getInventoryItemIds().contains(useInventoryItem.getInventoryId())) {
            throw new IllegalArgumentException("User does not own inventory item. Inventory Id: " + useInventoryItem.getInventoryId() + ". User: " + userId);
        }
        PlayerBaseFull playerBaseFull = baseItemService.getPlayerBaseFull4UserId(userId);
        try {
            baseItemService.useInventoryItem(useInventoryItem, playerBaseFull);
        } catch (ItemLimitExceededException | HouseSpaceExceededException e) {
            throw new RuntimeException(e);
        }
        userService.persistRemoveInventoryItem(userId, inventoryPersistence.getEntity(useInventoryItem.getInventoryId()));
        historyService.onInventoryItemUsed(userId, useInventoryItem.getInventoryId());
    }

    /**
     * Buy an inventory item for crystals (trader). Returns false if too few crystals.
     */
    @Transactional
    public boolean buyInventoryItem(String userId, int inventoryItemId) {
        var entity = inventoryPersistence.getEntity(inventoryItemId);
        boolean bought = userService.buyInventoryItem(userId, entity);
        if (bought) {
            historyService.onInventoryItemBought(userId, inventoryItemId, entity.getCrystalCost() != null ? entity.getCrystalCost() : 0);
        }
        return bought;
    }

    /**
     * Buy an artifact for crystals (trader). Returns false if too few crystals.
     */
    @Transactional
    public boolean buyInventoryArtifact(String userId, int inventoryArtifactId) {
        var entity = inventoryArtifactPersistence.getEntity(inventoryArtifactId);
        boolean bought = userService.buyInventoryArtifact(userId, entity);
        if (bought) {
            historyService.onInventoryArtifactBought(userId, inventoryArtifactId, entity.getCrystalCost() != null ? entity.getCrystalCost() : 0);
        }
        return bought;
    }

    /**
     * Assemble an inventory item from owned artifacts (workshop). Returns false if the user
     * does not own the required artifact set.
     */
    @Transactional
    public boolean assembleInventoryItem(String userId, int inventoryItemId) {
        boolean assembled = userService.assembleInventoryItem(userId, inventoryPersistence.getEntity(inventoryItemId));
        if (assembled) {
            historyService.onInventoryItemAssembled(userId, inventoryItemId);
        }
        return assembled;
    }
}
