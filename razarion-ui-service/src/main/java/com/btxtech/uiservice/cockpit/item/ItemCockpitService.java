package com.btxtech.uiservice.cockpit.item;


import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.Group;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.item.BaseItemUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ItemCockpitService {
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private GameUiControl gameUiControl;
    private ItemCockpitFrontend itemCockpitFrontend;
    // TODO private BuildupItemPanel buildupItemPanel;

    public void init(ItemCockpitFrontend itemCockpitPanel) {
        this.itemCockpitFrontend = itemCockpitPanel;
    }

    public void onOwnSelectionChanged(@Observes SelectionEvent selectionEvent) {
        switch (selectionEvent.getType()) {
            case CLEAR:
//    TODO            if (buildupItemPanel != null) {
//                    TODO buildupItemPanel.releaseMonitors();
//   TODO             }
                itemCockpitFrontend.dispose();
                break;
            case OWN:
                Group selectedGroup = selectionEvent.getSelectedGroup();
                itemCockpitFrontend.maximizeMinButton();
                Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> itemTypes = selectedGroup.getGroupedItems();
                if (selectedGroup.getCount() == 1) {
                    itemCockpitFrontend.displayOwnSingleType(1, createOwnItemCockpit(CollectionUtils.getFirst(itemTypes.keySet())));
                } else {
                    if (itemTypes.size() == 1) {
                        itemCockpitFrontend.displayOwnSingleType(itemTypes.size(), createOwnItemCockpit(CollectionUtils.getFirst(itemTypes.keySet())));
                    } else {
                        itemCockpitFrontend.displayOwnMultipleItemTypes(createOwnMultipleInfo(itemTypes));
                    }
                }
                break;
            case OTHER:
                itemCockpitFrontend.maximizeMinButton();
                itemCockpitFrontend.displayOtherItemType(createOtherInfo(selectionEvent.getSelectedOther()));
                break;
        }
    }

    private OwnItemCockpit createOwnItemCockpit(BaseItemType baseItemType) {
        OwnItemCockpit ownInfoPanel = createSimpleOwnItemCockpit(baseItemType);

        ownInfoPanel.sellButton = !gameUiControl.isSellSuppressed();
        ownInfoPanel.buildupItemInfos = createBuildupItemInfos(baseItemType);
        // TODO setupItemContainerPanel(syncBaseItem, baseItemType);

        return ownInfoPanel;
    }

    private OwnMultipleIteCockpit[] createOwnMultipleInfo(Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> itemTypes) {
        return itemTypes.entrySet().stream().map(entry -> {
            OwnMultipleIteCockpit ownMultipleItemInfo = new OwnMultipleIteCockpit();
            ownMultipleItemInfo.count = entry.getValue().size();
            ownMultipleItemInfo.ownItemCockpit = createSimpleOwnItemCockpit(entry.getKey());
            return ownMultipleItemInfo;
        }).toArray(OwnMultipleIteCockpit[]::new);
    }

    private OtherItemCockpit createOtherInfo(SyncItemSimpleDto otherSelection) {
        OtherItemCockpit otherInfoPanel = new OtherItemCockpit();
        ItemType itemType = null;
        if (otherSelection instanceof SyncBaseItemSimpleDto) {
            itemType = itemTypeService.getBaseItemType(otherSelection.getItemTypeId());
            SyncBaseItemSimpleDto syncBaseItem = (SyncBaseItemSimpleDto) otherSelection;
            PlayerBaseDto base = baseItemUiService.getBase(syncBaseItem.getBaseId());
            switch (base.getCharacter()) {
                case HUMAN:
                    if (base.getUserId() == null) {
                        otherInfoPanel.baseName = I18nHelper.getConstants().unregisteredUser();
                    } else if (base.getName() == null || base.getName().trim().isEmpty()) {
                        otherInfoPanel.baseName = I18nHelper.getConstants().unnamedUser();
                    } else {
                        otherInfoPanel.baseName = base.getName();
                    }
                    otherInfoPanel.type = I18nHelper.getConstants().playerFriend();
                    otherInfoPanel.friend = true;
                    break;
                case BOT:
                    otherInfoPanel.baseName = base.getName();
                    otherInfoPanel.type = I18nHelper.getConstants().botEnemy();
                    break;
                case BOT_NCP:
                    otherInfoPanel.baseName = base.getName();
                    otherInfoPanel.type = I18nHelper.getConstants().botNpc();
                    otherInfoPanel.friend = true;
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown character: " + base.getCharacter());
            }
        } else if (otherSelection instanceof SyncResourceItemSimpleDto) {
            itemType = itemTypeService.getResourceItemType(otherSelection.getItemTypeId());
        } else if (otherSelection instanceof SyncBoxItemSimpleDto) {
            itemType = itemTypeService.getBoxItemType(otherSelection.getItemTypeId());
        }
        if (itemType != null) {
            otherInfoPanel.imageUrl = CommonUrl.getImageServiceUrlSafe(itemType.getThumbnail());
            otherInfoPanel.itemTypeName = I18nHelper.getLocalizedString(itemType.getI18nName());
            otherInfoPanel.itemTypeDescr = I18nHelper.getLocalizedString(itemType.getI18nDescription());
        }
        return otherInfoPanel;
    }

    private BuildupItemCockpit[] createBuildupItemInfos(BaseItemType baseItemType) {
        List<Integer> ableToBuildId = null;
        if (baseItemType.getBuilderType() != null) {
            ableToBuildId = baseItemType.getBuilderType().getAbleToBuildIds();
        } else if (baseItemType.getFactoryType() != null) {
            ableToBuildId = baseItemType.getFactoryType().getAbleToBuildIds();
        }

        if (ableToBuildId == null || ableToBuildId.isEmpty()) {
            return null;
        }

        return ableToBuildId.stream()
                .filter(itemTypeId -> gameUiControl.getPlanetConfig().imitation4ItemType(itemTypeId) > 0)
                .map(itemTypeId -> {
                    BaseItemType itemType = itemTypeService.getBaseItemType(itemTypeId);
                    BuildupItemCockpit buildupItemInfo = new BuildupItemCockpit();
                    buildupItemInfo.imageUrl = CommonUrl.getImageServiceUrlSafe(itemType.getThumbnail());
                    buildupItemInfo.price = itemType.getPrice();
                    // TODO is changed form the game engine side
                    buildupItemInfo.itemCount = baseItemUiService.getMyItemCount(itemType.getId());
                    buildupItemInfo.itemLimit = gameUiControl.getMyLimitation4ItemType(itemType.getId());

                    if (baseItemUiService.isMyLevelLimitation4ItemTypeExceeded(itemType, 1)) {
                        buildupItemInfo.tooltip = I18nHelper.getConstants().tooltipNoBuildLimit(I18nHelper.getLocalizedString(itemType.getI18nName()));
                        buildupItemInfo.enabled = false;
                    } else if (baseItemUiService.isMyHouseSpaceExceeded(itemType, 1)) {
                        buildupItemInfo.tooltip = I18nHelper.getConstants().tooltipNoBuildHouseSpace(I18nHelper.getLocalizedString(itemType.getI18nName()));
                        buildupItemInfo.enabled = false;
                    }
                    if (itemType.getPrice() > baseItemUiService.getResources()) {
                        buildupItemInfo.tooltip = I18nHelper.getConstants().tooltipNoBuildMoney(I18nHelper.getLocalizedString(itemType.getI18nName()));
                        buildupItemInfo.enabled = false;
                    } else {
                        buildupItemInfo.tooltip = I18nHelper.getConstants().tooltipBuild(I18nHelper.getLocalizedString(itemType.getI18nName()));
                        buildupItemInfo.enabled = true;

                    }
//       TODO             buildupItemInfos.add(setupBuildupBlock(itemType, () -> {
//                        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemCount(1).setBaseItemTypeId(itemTypeId);
//                        baseItemPlacerService.activate(baseItemPlacerConfig, true, decimalPositions -> {
//                            audioService.onCommandSent();
//                            gameEngineControl.buildCmd(constructionVehicles.getFirst(), CollectionUtils.getFirst(decimalPositions), itemType);
//                        });
//                    }));
                    return buildupItemInfo;
                }).toArray(BuildupItemCockpit[]::new);
    }

    private OwnItemCockpit createSimpleOwnItemCockpit(BaseItemType baseItemType) {
        OwnItemCockpit ownInfoPanel = new OwnItemCockpit();
        ownInfoPanel.imageUrl = CommonUrl.getImageServiceUrlSafe(baseItemType.getThumbnail());
        ownInfoPanel.itemTypeName = I18nHelper.getLocalizedString(baseItemType.getI18nName());
        ownInfoPanel.itemTypeDescr = I18nHelper.getLocalizedString(baseItemType.getI18nDescription());
        return ownInfoPanel;
    }

//   TODO private void setupItemContainerPanel(SyncBaseItemSimpleDto syncBaseItem, BaseItemType baseItemType) {
//        if (baseItemType.getItemContainerType() != null) {
//            itemContainerPanel = instance.select(ItemContainerPanel.class).get();
//            itemContainerPanel.display(syncBaseItem);
//            itemCockpitFrontend.setItemContainerPanel(itemContainerPanel);
//        } else {
//            itemContainerPanel = null;
//        }
//    }

    public Rectangle getBuildButtonLocation(int baseItemTypeId) {
//  TODO      if (buildupItemPanel == null) {
//            throw new IllegalStateException("No buildup item panel");
//        }
//        return buildupItemPanel.getBuildButtonLocation(baseItemTypeId);
        throw new UnsupportedOperationException("...TODO...");
    }

    public void onResourcesChanged(int resources) {
//  TODO      if (buildupItemPanel != null) {
//            buildupItemPanel.onResourcesChanged(resources);
//        }
    }

    // This method is may not called enough. Only called on Level change and houseSpace and usedHouseSpace changed
    // If an item is create and and item of a different item type is killed, this is method not called
    public void onStateChanged() {
//  TODO      if (buildupItemPanel != null) {
//            buildupItemPanel.onStateChanged();
//        }
    }
}
