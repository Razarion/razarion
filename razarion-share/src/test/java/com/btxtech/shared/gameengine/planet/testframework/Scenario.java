package com.btxtech.shared.gameengine.planet.testframework;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 12.04.2018.
 */
public class Scenario {
    private String fileName;
    private Class theClass;
    private PlayerBaseFull playerBase1;
    private ItemTypeService itemTypeService;
    private BaseItemService baseItemService;
    private PathingService pathingService;
    private List<SyncBaseItem> createdSyncBaseItems = new ArrayList<>();
    private Runnable saveCallback;

    public Scenario(String fileName, Class theClass) {
        this.fileName = fileName;
        this.theClass = theClass;
    }

    final public void setup(PlayerBaseFull playerBase1, ItemTypeService itemTypeService, BaseItemService baseItemService, PathingService pathingService) {
        this.playerBase1 = playerBase1;
        this.itemTypeService = itemTypeService;
        this.baseItemService = baseItemService;
        this.pathingService = pathingService;
    }

    public void setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
    }

    // Override in subclasses
    protected void createSyncItems() {

    }

    final protected SyncBaseItem createSyncBaseItemSimplePath(int baseItemTypeId, DecimalPosition position, DecimalPosition destination) {
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

    final protected SyncBaseItem createSyncBaseItem(int baseItemTypeId, DecimalPosition position, DecimalPosition destination) {
        try {
            SyncBaseItem syncBaseItem = baseItemService.spawnSyncBaseItem(itemTypeService.getBaseItemType(baseItemTypeId), position, 0, playerBase1, true);
            if (syncBaseItem.getSyncPhysicalArea().canMove() && destination != null) {
                SimplePath path = pathingService.setupPathToDestination(syncBaseItem, destination);
                ((SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea()).setPath(path);
            }
            createdSyncBaseItems.add(syncBaseItem);
            return syncBaseItem;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    final protected void createSyncBaseItemGroup(int baseItemTypeId, int edgeCount, DecimalPosition start, DecimalPosition destination) {
        for (int x = -edgeCount / 2; x < Math.round(edgeCount / 2.0); x++) {
            for (int y = -edgeCount / 2; y < Math.round(edgeCount / 2.0); y++) {
                createSyncBaseItem(baseItemTypeId, new DecimalPosition(4 * x + start.getX(), 4 * y + start.getY()), destination);
            }
        }
    }

    public String getFileName() {
        return fileName;
    }

    public ScenarioTicks readExpectedTicks() throws IOException {
        InputStream inputStream = theClass.getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IOException("PATH IS WRONG: Resource does not exist: " + theClass.getProtectionDomain().getCodeSource().getLocation().getPath() + fileName);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(inputStream, ScenarioTicks.class);
    }

    public void save(String saveDir, ScenarioTicks expectedScenarioTicks) {
        try {
            File savePath = new File(saveDir, fileName);
            System.out.println("Save expected text case values to: " + savePath);
            new ObjectMapper().writeValue(savePath, expectedScenarioTicks);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    public void onSave() {
        if (saveCallback != null) {
            saveCallback.run();
        }
    }
}
