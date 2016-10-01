package com.btxtech.uiservice.cockpit.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Group;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.uiservice.cockpit.CockpitMode;
import com.btxtech.uiservice.storyboard.StoryboardService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 29.09.2016.
 */
public abstract class BuildupItemPanel {
    @Inject
    private CockpitMode cockpitMode;
    @Inject
    private StoryboardService storyboardService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private CommandService commandService;
    private Group selectedGroup;
    private Map<Integer, BuildupItem> buildupItems = new HashMap<>();

    protected abstract void clear();

    protected abstract void setBuildupItem(List<BuildupItem> buildupItems);

    public void display(SyncBaseItem syncBaseItem) {
        selectedGroup = null;
        if (syncBaseItem.hasSyncBuilder()) {
            Group group = new Group();
            group.addItem(syncBaseItem);
            selectedGroup = group;
            setupBuildupItemsCV(group);
        } else if (syncBaseItem.hasSyncFactory()) {
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

    private void setupBuildupItemsCV(final Group constructionVehicles) throws NoSuchItemTypeException {
        clear();
        Collection<Integer> itemTypeIds = constructionVehicles.getFirst().getBaseItemType().getBuilderType().getAbleToBuild();
        List<BuildupItem> buildupItems = new ArrayList<>();
        for (Integer itemTypeId : itemTypeIds) {
            if (storyboardService.getMyLimitation4ItemType(itemTypeId) == 0) {
                continue;
            }
            BaseItemType itemType = itemTypeService.getBaseItemType(itemTypeId);
            buildupItems.add(setupBuildupBlock(itemType, position -> cockpitMode.setToBeBuildPlacer(itemType, constructionVehicles, position)));
        }
        setBuildupItem(buildupItems);
    }

    private void setupBuildupItemsFactory(final Group factories) throws NoSuchItemTypeException {
        clear();
        Collection<Integer> itemTypeIds = factories.getFirst().getBaseItemType().getFactoryType().getAbleToBuild();
        List<BuildupItem> buildupItems = new ArrayList<>();
        for (Integer itemTypeId : itemTypeIds) {
            if (storyboardService.getMyLimitation4ItemType(itemTypeId) == 0) {
                continue;
            }
            BaseItemType itemType = itemTypeService.getBaseItemType(itemTypeId);
            buildupItems.add(setupBuildupBlock(itemType, position -> commandService.fabricate(factories.getItems(), itemType)));
        }
        setBuildupItem(buildupItems);
    }

    private BuildupItem setupBuildupBlock(BaseItemType itemType, Consumer<DecimalPosition> callback) {
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

//    public Index getAbsoluteMiddleTopPosition(int buildupItemTypeId) {
//        BuildupItem buildupItem = buildupItems.get(buildupItemTypeId);
//        if (buildupItem == null) {
//            throw new IllegalArgumentException("BuildupItemPanel.getAbsoluteMiddleTopPosition() buildupItemTypeId is not known: " + buildupItemTypeId);
//        }
//        return new Index(buildupItem.getAbsoluteLeft() + buildupItem.getOffsetWidth() / 2, buildupItem.getAbsoluteTop());
//    }

}
