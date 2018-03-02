package com.btxtech.uiservice.cockpit.item;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.Group;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.SyncBaseItemMonitor;
import com.btxtech.uiservice.item.SyncItemMonitor;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 29.09.2016.
 */
public abstract class BuildupItemPanel {
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private AudioService audioService;
    @Inject
    private Instance<Group> groupInstance;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private GameUiControl gameUiControl;
    private Group selectedGroup;
    private Map<Integer, BuildupItem> buildupItems = new HashMap<>();
    private boolean hasItemsToBuild;
    private SyncBaseItemMonitor syncBaseItemMonitor;

    protected abstract void clear();

    protected abstract void setBuildupItem(List<BuildupItem> buildupItems);

    protected abstract Rectangle getBuildButtonLocation(BuildupItem buildupItem);

    public abstract void onResourcesChanged(int resources);

    public void display(SyncBaseItemSimpleDto syncBaseItem) {
        selectedGroup = null;
        BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
        if (baseItemType.getBuilderType() != null) {
            Group group = groupInstance.get();
            group.addItem(syncBaseItem);
            selectedGroup = group;
            setupBuildupItemsCV(group);
        } else if (baseItemType.getFactoryType() != null) {
            Group group = groupInstance.get();
            group.addItem(syncBaseItem);
            selectedGroup = group;
            setupBuildupItemsFactory(group);
        }
        releaseMonitors();
        syncBaseItemMonitor = baseItemUiService.monitorSyncItem(syncBaseItem.getId());
        syncBaseItemMonitor.setConstructingChangeListener(syncItemMonitor -> {
            buildupItems.forEach((constructingBaseItemTypeId, buildupItem) -> {
                SyncBaseItemMonitor syncBaseItemMonitor = (SyncBaseItemMonitor) syncItemMonitor;
                if (syncBaseItemMonitor.getConstructingBaseItemTypeId() != null && constructingBaseItemTypeId.equals(syncBaseItemMonitor.getConstructingBaseItemTypeId())) {
                    buildupItem.setConstructing(syncBaseItemMonitor.getConstructing());
                } else {
                    buildupItem.setConstructing(null);
                }
            });
        });
    }

    void display(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
        if (selectedGroup.onlyConstructionVehicle()) {
            setupBuildupItemsCV(selectedGroup);
        } else if (selectedGroup.onlyFactories()) {
            setupBuildupItemsFactory(selectedGroup);
        }
    }

    private void setupBuildupItemsCV(Group constructionVehicles) throws NoSuchItemTypeException {
        clear();
        hasItemsToBuild = false;
        Collection<Integer> itemTypeIds = itemTypeService.getBaseItemType(constructionVehicles.getFirst().getItemTypeId()).getBuilderType().getAbleToBuildIds();
        List<BuildupItem> buildupItems = new ArrayList<>();
        for (Integer itemTypeId : itemTypeIds) {
            if (gameUiControl.getPlanetConfig().imitation4ItemType(itemTypeId) == 0) {
                continue;
            }
            hasItemsToBuild = true;
            BaseItemType itemType = itemTypeService.getBaseItemType(itemTypeId);
            buildupItems.add(setupBuildupBlock(itemType, () -> {
                BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemCount(1).setBaseItemTypeId(itemTypeId);
                baseItemPlacerService.activate(baseItemPlacerConfig, true, decimalPositions -> {
                    audioService.onCommandSent();
                    gameEngineControl.buildCmd(constructionVehicles.getFirst(), CollectionUtils.getFirst(decimalPositions), itemType);
                });
            }));
        }
        setBuildupItem(buildupItems);
    }

    private void setupBuildupItemsFactory(Group factories) throws NoSuchItemTypeException {
        clear();
        hasItemsToBuild = false;
        Collection<Integer> itemTypeIds = itemTypeService.getBaseItemType(factories.getFirst().getItemTypeId()).getFactoryType().getAbleToBuildIds();
        List<BuildupItem> buildupItems = new ArrayList<>();
        for (Integer itemTypeId : itemTypeIds) {
            if (gameUiControl.getPlanetConfig().imitation4ItemType(itemTypeId) == 0) {
                continue;
            }
            hasItemsToBuild = true;
            BaseItemType itemType = itemTypeService.getBaseItemType(itemTypeId);
            buildupItems.add(setupBuildupBlock(itemType, () -> {
                audioService.onCommandSent();
                factories.getSyncBaseItemsMonitors().stream().filter(syncBaseItemMonitor -> syncBaseItemMonitor.getConstructingBaseItemTypeId() == null).map(SyncItemMonitor::getSyncItemId).findFirst().ifPresent(factoryId -> gameEngineControl.fabricateCmd(factoryId, itemType));
            }));
        }
        setBuildupItem(buildupItems);
    }

    private BuildupItem setupBuildupBlock(BaseItemType itemType, Runnable callback) {
        BuildupItem buildupItem = new BuildupItem(itemType, callback);
        this.buildupItems.put(itemType.getId(), buildupItem);
        return buildupItem;
    }

    public void onStateChanged() {
        display(selectedGroup);
    }

    boolean isHasItemsToBuild() {
        return hasItemsToBuild;
    }

    Rectangle getBuildButtonLocation(int baseItemTypeId) {
        BuildupItem buildupItem = buildupItems.get(baseItemTypeId);
        if (buildupItem == null) {
            throw new IllegalArgumentException("No BuildupItem for: " + baseItemTypeId);
        }
        return getBuildButtonLocation(buildupItem);
    }

    public void releaseMonitors() {
        if (syncBaseItemMonitor != null) {
            syncBaseItemMonitor.release();
            syncBaseItemMonitor = null;
        }
    }
}
