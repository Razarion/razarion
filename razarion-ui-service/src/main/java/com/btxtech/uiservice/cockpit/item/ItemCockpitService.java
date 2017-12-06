package com.btxtech.uiservice.cockpit.item;


import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.Group;
import com.btxtech.uiservice.SelectionEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

@ApplicationScoped
public class ItemCockpitService {
    @Inject
    private Instance<Object> instance;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ItemCockpitPanel itemCockpitPanel;
    @Inject
    private ItemTypeService itemTypeService;
    private BuildupItemPanel buildupItemPanel;
    private ItemContainerPanel itemContainerPanel;
    private boolean isActive = false;

    public boolean isActive() {
        return isActive;
    }

    public void onOwnSelectionChanged(@Observes SelectionEvent selectionEvent) {
        switch (selectionEvent.getType()) {
            case CLEAR:
                itemCockpitPanel.cleanPanels();
                if (isActive) {
                    isActive = false;
                    itemCockpitPanel.showPanel(false);
                }
                break;
            case OWN:
                Group selectedGroup = selectionEvent.getSelectedGroup();
                itemCockpitPanel.maximizeMinButton();
                itemCockpitPanel.cleanPanels();
                if (selectedGroup.getCount() == 1) {
                    activeOwnSingle(selectedGroup.getFirst());
                } else {
                    Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> itemTypes = selectedGroup.getGroupedItems();
                    if (itemTypes.size() == 1) {
                        activeOwnMultiSameType(CollectionUtils.getFirst(itemTypes.keySet()), selectedGroup);
                    } else {
                        OwnMultiDifferentItemPanel multiDifferentItemPanel = instance.select(OwnMultiDifferentItemPanel.class).get();
                        multiDifferentItemPanel.init(itemTypes);
                        itemCockpitPanel.setInfoPanel(multiDifferentItemPanel);
                    }
                }
                isActive = true;
                itemCockpitPanel.showPanel(true);
                break;
            case OTHER:
                itemCockpitPanel.maximizeMinButton();
                itemCockpitPanel.cleanPanels();
                OtherInfoPanel otherInfoPanel = instance.select(OtherInfoPanel.class).get();
                otherInfoPanel.init(selectionEvent.getSelectedOther());
                itemCockpitPanel.setInfoPanel(otherInfoPanel);
                isActive = true;
                itemCockpitPanel.showPanel(true);
                break;
        }
    }

    private void activeOwnSingle(SyncBaseItemSimpleDto syncBaseItem) {
        OwnInfoPanel ownInfoPanel = instance.select(OwnInfoPanel.class).get();
        BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
        ownInfoPanel.init(baseItemType, 1);
        itemCockpitPanel.setInfoPanel(ownInfoPanel);
        setupBuildupPanel(syncBaseItem, baseItemType);
        setupItemContainerPanel(syncBaseItem, baseItemType);
    }

    private void setupBuildupPanel(SyncBaseItemSimpleDto syncBaseItem, BaseItemType baseItemType) {
        if (baseItemType.getFactoryType() != null || baseItemType.getBuilderType() != null) {
            buildupItemPanel = instance.select(BuildupItemPanel.class).get();
            buildupItemPanel.display(syncBaseItem);
            if (buildupItemPanel.isHasItemsToBuild()) {
                itemCockpitPanel.setBuildupItemPanel(buildupItemPanel);
            } else {
                buildupItemPanel = null;
            }
        }
    }

    private void setupItemContainerPanel(SyncBaseItemSimpleDto syncBaseItem, BaseItemType baseItemType) {
        if (baseItemType.getItemContainerType() != null) {
            itemContainerPanel = instance.select(ItemContainerPanel.class).get();
            itemContainerPanel.display(syncBaseItem);
            itemCockpitPanel.setItemContainerPanel(itemContainerPanel);
        } else {
            itemContainerPanel = null;
        }
    }

    private void activeOwnMultiSameType(BaseItemType baseItemType, Group group) {
        OwnInfoPanel ownInfoPanel = instance.select(OwnInfoPanel.class).get();
        ownInfoPanel.init(baseItemType, group.getCount());
        itemCockpitPanel.setInfoPanel(ownInfoPanel);
        if (baseItemType.getFactoryType() != null || baseItemType.getBuilderType() != null) {
            buildupItemPanel = instance.select(BuildupItemPanel.class).get();
            buildupItemPanel.display(group);
            if (buildupItemPanel.isHasItemsToBuild()) {
                itemCockpitPanel.setBuildupItemPanel(buildupItemPanel);
            } else {
                buildupItemPanel = null;
            }
        }
    }

    public Rectangle getBuildButtonLocation(int baseItemTypeId) {
        if (buildupItemPanel == null) {
            throw new IllegalStateException("No buildup item panel");
        }
        return buildupItemPanel.getBuildButtonLocation(baseItemTypeId);
    }

    public void onResourcesChanged(int resources) {
        if (buildupItemPanel != null) {
            buildupItemPanel.onResourcesChanged(resources);
        }
    }

    // This method is may not called enough. Only called on Level change and houseSpace and usedHouseSpace changed
    // If an item is create and and item of a different item type is killed, this is method not called
    public void onStateChanged() {
        if (buildupItemPanel != null) {
            buildupItemPanel.onStateChanged();
        }
    }
}
