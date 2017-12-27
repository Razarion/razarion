package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UnregisteredUser;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
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
    private InventoryPersistence inventoryPersistence;
    @Inject
    private SessionService sessionService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private HistoryPersistence historyPersistence;

    public void onBoxPicked(HumanPlayerId humanPlayerId, BoxContent boxContent) {
        if (humanPlayerId.getUserId() != null) {
            if (boxContent.getCrystals() > 0) {
                userService.persistAddCrystals(humanPlayerId.getUserId(), boxContent.getCrystals());
            }
            boxContent.getInventoryItems().forEach(inventoryItem -> userService.persistAddInventoryItem(humanPlayerId.getUserId(), inventoryPersistence.readInventoryItemEntity(inventoryItem.getId())));
        } else {
            UnregisteredUser unregisteredUser = sessionService.findPlayerSession(humanPlayerId).getUnregisteredUser();
            if (unregisteredUser != null) {
                if (boxContent.getCrystals() > 0) {
                    unregisteredUser.addCrystals(boxContent.getCrystals());
                }
                boxContent.getInventoryItems().forEach(inventoryItem -> unregisteredUser.addInventoryItemId(inventoryItem.getId()));
            }
        }
        historyPersistence.onBoxPicked(humanPlayerId, boxContent);
    }

    public InventoryInfo loadInventory(PlayerSession playerSession) {
        if (playerSession.getUserContext().checkRegistered()) {
            return userService.readInventoryInfo(playerSession.getUserContext().getHumanPlayerId().getUserId());
        } else {
            return playerSession.getUnregisteredUser().toInventoryInfo();
        }
    }

    public int loadCrystals(PlayerSession playerSession) {
        if (playerSession.getUserContext().checkRegistered()) {
            return userService.readCrystals(playerSession.getUserContext().getHumanPlayerId().getUserId());
        } else {
            return playerSession.getUnregisteredUser().getCrystals();
        }
    }

    public void useInventoryItem(UseInventoryItem useInventoryItem, PlayerSession playerSession, PlayerBaseFull playerBaseFull) {
        InventoryInfo inventoryInfo = loadInventory(playerSession);
        if (inventoryInfo.getInventoryItemIds() == null || !inventoryInfo.getInventoryItemIds().contains(useInventoryItem.getInventoryId())) {
            throw new IllegalArgumentException("User does not have inventory. Inventory Id: " + useInventoryItem.getInventoryId() + ". HumanPlayerId: " + playerSession.getUserContext().getHumanPlayerId());
        }
        if (playerSession.getUserContext().checkRegistered()) {
            userService.persistRemoveInventoryItem(playerSession.getUserContext().getHumanPlayerId().getUserId(), inventoryPersistence.readInventoryItemEntity(useInventoryItem.getInventoryId()));
        } else {
            UnregisteredUser unregisteredUser = sessionService.findPlayerSession(playerSession.getUserContext().getHumanPlayerId()).getUnregisteredUser();
            if (unregisteredUser != null) {
                unregisteredUser.removeInventoryItemId(useInventoryItem.getInventoryId());
            }
        }
        baseItemService.useInventoryItem(useInventoryItem, playerBaseFull);
        historyPersistence.onInventoryItemUsed(playerSession.getUserContext().getHumanPlayerId(), useInventoryItem.getInventoryId());
    }
}
