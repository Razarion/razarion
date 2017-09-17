package com.btxtech.server.user;

import com.btxtech.server.persistence.inventory.InventoryPersistence;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;

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

    public void onBoxPicked(HumanPlayerId humanPlayerId, BoxContent boxContent) {
        if(humanPlayerId.getUserId() != null) {
            boxContent.getInventoryItems().forEach(inventoryItem -> {
                userService.persistAddInventoryItem(humanPlayerId.getUserId(), inventoryPersistence.readInventoryItemEntity(inventoryItem.getId()));
            });
        } else {

        }

        UserContext userContext = userService.getUserContext(humanPlayerId);
        boxContent.getInventoryItems().forEach(inventoryItem -> userContext.addInventoryItem(inventoryItem.getId()));

        // TODO Registered


        // TODO unregistered
    }
}
