package com.btxtech.uiservice.cockpit.item;


import com.btxtech.shared.datatypes.Group;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.utils.CollectionUtils;
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
    private BuildupItemPanel buildupItemPanel;
    private boolean isActive = false;

    public boolean isActive() {
        return isActive;
    }

    public void onMoneyChanged(double accountBalance) {
        // TODO
    }

    public void onStateChanged() {
        // TODO
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
                    Map<BaseItemType, Collection<SyncBaseItem>> itemTypes = selectedGroup.getGroupedItems();
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
            case TRAGET:
                itemCockpitPanel.maximizeMinButton();
                itemCockpitPanel.cleanPanels();
                OtherInfoPanel otherInfoPanel = instance.select(OtherInfoPanel.class).get();
                otherInfoPanel.init(selectionEvent.getTargetSelection());
                itemCockpitPanel.setInfoPanel(otherInfoPanel);
                isActive = true;
                itemCockpitPanel.showPanel(true);
                break;
        }
    }

    private void activeOwnSingle(SyncBaseItem syncBaseItem) {
        OwnInfoPanel ownInfoPanel = instance.select(OwnInfoPanel.class).get();
        ownInfoPanel.init(syncBaseItem.getBaseItemType(), 1);
        itemCockpitPanel.setInfoPanel(ownInfoPanel);
//    TODO    if (SpecialFunctionPanel.hasSpecialFuntion(syncBaseItem)) {
//    TODO        specialFunctionPanel.setWidget(new SpecialFunctionPanel(syncBaseItem));
//    TODO        specialFunctionPanel.setVisible(true);
//    TODO    }
        if (syncBaseItem.hasSyncFactory() || syncBaseItem.hasSyncBuilder()) {
            buildupItemPanel = instance.select(BuildupItemPanel.class).get();
            buildupItemPanel.display(syncBaseItem);
            itemCockpitPanel.setBuildupItemPanel(buildupItemPanel);
        }
    }

    private void activeOwnMultiSameType(BaseItemType baseItemType, Group group) {
        OwnInfoPanel ownInfoPanel = instance.select(OwnInfoPanel.class).get();
        ownInfoPanel.init(baseItemType, group.getCount());
        itemCockpitPanel.setInfoPanel(ownInfoPanel);
        if (baseItemType.getFactoryType() != null || baseItemType.getBuilderType() != null) {
            buildupItemPanel = instance.select(BuildupItemPanel.class).get();
            buildupItemPanel.display(group);
            itemCockpitPanel.setBuildupItemPanel(buildupItemPanel);
        }
    }

}
