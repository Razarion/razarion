package com.btxtech.uiservice.cockpit.item;

import com.btxtech.shared.datatypes.Group;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.storyboard.StoryboardService;

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
    private StoryboardService storyboardService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private CommandService commandService;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private AudioService audioService;
    private Group selectedGroup;
    private Map<Integer, BuildupItem> buildupItems = new HashMap<>();
    private boolean hasItemsToBuild;

    protected abstract void clear();

    protected abstract void setBuildupItem(List<BuildupItem> buildupItems);

    protected abstract Rectangle getBuildButtonLocation(BuildupItem buildupItem);

    public void display(SyncBaseItem syncBaseItem) {
        selectedGroup = null;
        if (syncBaseItem.getSyncBuilder() != null) {
            Group group = new Group();
            group.addItem(syncBaseItem);
            selectedGroup = group;
            setupBuildupItemsCV(group);
        } else if (syncBaseItem.getSyncFactory() != null) {
            Group group = new Group();
            group.addItem(syncBaseItem);
            selectedGroup = group;
            setupBuildupItemsFactory(group);
        }
    }

    public void display(Group selectedGroup) {
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
        Collection<Integer> itemTypeIds = constructionVehicles.getFirst().getBaseItemType().getBuilderType().getAbleToBuild();
        List<BuildupItem> buildupItems = new ArrayList<>();
        for (Integer itemTypeId : itemTypeIds) {
            if (storyboardService.getMyLimitation4ItemType(itemTypeId) == 0) {
                continue;
            }
            hasItemsToBuild = true;
            BaseItemType itemType = itemTypeService.getBaseItemType(itemTypeId);
            BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemCount(1).setBaseItemTypeId(itemTypeId);
            buildupItems.add(setupBuildupBlock(itemType, () -> baseItemPlacerService.activate(baseItemPlacerConfig, decimalPositions -> {
                audioService.onCommandSent();
                commandService.build(constructionVehicles.getFirst(), CollectionUtils.getFirst(decimalPositions), itemType);
            })));
        }
        setBuildupItem(buildupItems);
    }

    private void setupBuildupItemsFactory(Group factories) throws NoSuchItemTypeException {
        clear();
        hasItemsToBuild = false;
        Collection<Integer> itemTypeIds = factories.getFirst().getBaseItemType().getFactoryType().getAbleToBuildId();
        List<BuildupItem> buildupItems = new ArrayList<>();
        for (Integer itemTypeId : itemTypeIds) {
            if (storyboardService.getMyLimitation4ItemType(itemTypeId) == 0) {
                continue;
            }
            hasItemsToBuild = true;
            BaseItemType itemType = itemTypeService.getBaseItemType(itemTypeId);
            buildupItems.add(setupBuildupBlock(itemType, () -> {
                audioService.onCommandSent();
                commandService.fabricate(factories.getItems(), itemType);
            }));
        }
        setBuildupItem(buildupItems);
    }

    private BuildupItem setupBuildupBlock(BaseItemType itemType, Runnable callback) {
        BuildupItem buildupItem = new BuildupItem(itemType, callback);
        this.buildupItems.put(itemType.getId(), buildupItem);
        return buildupItem;
    }

    public void onMoneyChanged(double accountBalance) {
        for (BuildupItem buildupItem : this.buildupItems.values()) {
            buildupItem.onMoneyChanged(accountBalance);
        }
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
}
