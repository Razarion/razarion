package com.btxtech.webglemulator.razarion.cockpit.item;

import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.uiservice.cockpit.item.OtherInfoPanel;
import com.btxtech.uiservice.item.BaseItemUiService;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

/**
 * Created by Beat
 * 07.10.2016.
 */
public class DevToolOtherInfoPanel extends VBox implements OtherInfoPanel {
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemUiService baseItemUiService;

    @Override
    public void init(SyncItemSimpleDto otherSelection) {
        getChildren().add(new Label("OtherInfoPanel"));
        if (otherSelection instanceof SyncBaseItemSimpleDto) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(otherSelection.getItemTypeId());
            getChildren().add(new Label("Type: " + baseItemType.getInternalName()));
            getChildren().add(new Label("Description: " + baseItemType.getI18nDescription()));
            getChildren().add(new Label("Player: " + baseItemUiService.getBase(((SyncBaseItemSimpleDto) otherSelection).getBaseId()).getName()));
        } else if (otherSelection instanceof SyncResourceItemSimpleDto) {
            ResourceItemType resourceItemType = itemTypeService.getResourceItemType(otherSelection.getItemTypeId());
            getChildren().add(new Label("Type: " + resourceItemType.getInternalName()));
            getChildren().add(new Label("Description: " + resourceItemType.getI18nDescription()));
        } else if (otherSelection instanceof SyncBoxItemSimpleDto) {
            BoxItemType boxItemType = itemTypeService.getBoxItemType(otherSelection.getItemTypeId());
            getChildren().add(new Label("Type: " + boxItemType.getInternalName()));
            getChildren().add(new Label("Description: " + boxItemType.getI18nDescription()));
        }
    }
}
