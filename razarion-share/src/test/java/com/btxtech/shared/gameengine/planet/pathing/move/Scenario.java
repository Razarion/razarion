package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 12.04.2018.
 */
public class Scenario {
    private String fileName;
    private PlayerBaseFull playerBase1;
    private ItemTypeService itemTypeService;
    private BaseItemService baseItemService;
    private List<SyncBaseItem> createdSyncBaseItems = new ArrayList<>();

    public Scenario(String fileName) {
        this.fileName = fileName;
    }

    final public void setup(PlayerBaseFull playerBase1, ItemTypeService itemTypeService, BaseItemService baseItemService) {
        this.playerBase1 = playerBase1;
        this.itemTypeService = itemTypeService;
        this.baseItemService = baseItemService;
    }

    // Override in subclasses
    protected void createSyncItems() {

    }

    final protected SyncBaseItem createSyncBaseItem(int baseItemTypeId, DecimalPosition position, DecimalPosition destination) {
        try {
            SyncBaseItem syncBaseItem = baseItemService.spawnSyncBaseItem(itemTypeService.getBaseItemType(baseItemTypeId), position, 0, playerBase1, true);
            if (syncBaseItem.getSyncPhysicalArea().canMove() && destination != null) {
                SimplePath path = new SimplePath();
                path.setWayPositions(Collections.singletonList(destination));
                ((SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea()).setPath(path);
            }
            createdSyncBaseItems.add(syncBaseItem);
            return syncBaseItem;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getFileName() {
        return fileName;
    }
}
