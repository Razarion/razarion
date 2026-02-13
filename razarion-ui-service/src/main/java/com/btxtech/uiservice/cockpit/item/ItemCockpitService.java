package com.btxtech.uiservice.cockpit.item;


import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
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
import com.btxtech.uiservice.SelectionEventService;
import com.btxtech.uiservice.SelectionService;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.SyncBaseItemMonitor;
import com.btxtech.uiservice.item.SyncItemMonitor;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ItemCockpitService {
    private final Logger logger = Logger.getLogger(ItemCockpitService.class.getName());
    private final ItemTypeService itemTypeService;
    private final Provider<BaseItemUiService> baseItemUiService;
    private final Provider<GameUiControl> gameUiControl;
    private final BaseItemPlacerService baseItemPlacerService;
    private final Provider<GameEngineControl> gameEngineControl;
    private final SelectionService selectionService;
    private final Collection<BuildupItemCockpit> buildupItemCockpits = new ArrayList<>();
    private ItemCockpitFrontend itemCockpitFrontend;

    @Inject
    public ItemCockpitService(SelectionService selectionService,
                              Provider<GameEngineControl> gameEngineControl,
                              BaseItemPlacerService baseItemPlacerService,
                              Provider<GameUiControl> gameUiControl,
                              Provider<BaseItemUiService> baseItemUiService,
                              ItemTypeService itemTypeService,
                              SelectionEventService selectionEventService) {
        this.selectionService = selectionService;
        this.gameEngineControl = gameEngineControl;
        this.baseItemPlacerService = baseItemPlacerService;
        this.gameUiControl = gameUiControl;
        this.baseItemUiService = baseItemUiService;
        this.itemTypeService = itemTypeService;
        selectionEventService.receiveSelectionEvent(this::onOwnSelectionChanged);
    }

    public void init(ItemCockpitFrontend itemCockpitFrontend) {
        this.itemCockpitFrontend = itemCockpitFrontend;
    }

    private void onOwnSelectionChanged(SelectionEvent selectionEvent) {
        switch (selectionEvent.getType()) {
            case CLEAR:
                buildupItemCockpits.forEach(BuildupItemCockpit::releaseMonitor); // TODO releaseMonitor
                buildupItemCockpits.clear();
                itemCockpitFrontend.dispose();
                break;
            case OWN:
                Group selectedGroup = selectionEvent.getSelectedGroup();
                Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> itemTypes = selectedGroup.getGroupedItems();
                if (selectedGroup.getCount() == 1) {
                    itemCockpitFrontend.displayOwnSingleType(1, createOwnItemCockpit(CollectionUtils.getFirst(itemTypes.keySet()), selectedGroup));
                } else {
                    if (itemTypes.size() == 1) {
                        itemCockpitFrontend.displayOwnSingleType(itemTypes.size(), createOwnItemCockpit(CollectionUtils.getFirst(itemTypes.keySet()), selectedGroup));
                    } else {
                        itemCockpitFrontend.displayOwnMultipleItemTypes(createOwnMultipleInfo(itemTypes));
                    }
                }
                break;
            case OTHER:
                itemCockpitFrontend.displayOtherItemType(createOtherInfo(selectionEvent.getSelectedOther()));
                break;
        }
    }

    private OwnItemCockpit createOwnItemCockpit(BaseItemType baseItemType, Group selectedGroup) {
        OwnItemCockpit ownInfoPanel = createSimpleOwnItemCockpit(baseItemType, selectedGroup.getItems());

        ownInfoPanel.buildupItemInfos = createBuildupItemInfos(baseItemType, selectedGroup);

        ownInfoPanel.itemContainerInfo = createItemContainerInfo(baseItemType, selectedGroup);

        return ownInfoPanel;
    }

    private OwnMultipleIteCockpit[] createOwnMultipleInfo(Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> itemTypes) {
        return itemTypes.entrySet().stream().map(entry -> {
            OwnMultipleIteCockpit ownMultipleItemInfo = new OwnMultipleIteCockpit() {
                @Override
                public void onSelect() {
                    try {
                        selectionService.keepOnlyOwnOfType(entry.getKey());
                    } catch (Throwable t) {
                        logger.log(Level.WARNING, t.getMessage(), t);
                    }
                }
            };
            ownMultipleItemInfo.count = entry.getValue().size();
            ownMultipleItemInfo.ownItemCockpit = createSimpleOwnItemCockpit(entry.getKey(), entry.getValue());
            return ownMultipleItemInfo;
        }).toArray(OwnMultipleIteCockpit[]::new);
    }

    private OtherItemCockpit createOtherInfo(SyncItemSimpleDto otherSelection) {
        OtherItemCockpit otherInfoPanel = new OtherItemCockpit();
        otherInfoPanel.id = otherSelection.getId();
        ItemType itemType = null;
        if (otherSelection instanceof SyncBaseItemSimpleDto) {
            itemType = itemTypeService.getBaseItemType(otherSelection.getItemTypeId());
            SyncBaseItemSimpleDto syncBaseItem = (SyncBaseItemSimpleDto) otherSelection;
            PlayerBaseDto base = baseItemUiService.get().getBase(syncBaseItem.getBaseId());
            otherInfoPanel.baseId = base.getBaseId();
            switch (base.getCharacter()) {
                case HUMAN:
                    if (base.getUserId() == null) {
                        otherInfoPanel.baseName = "Unregistered user";
                    } else if (base.getName() == null || base.getName().trim().isEmpty()) {
                        otherInfoPanel.baseName = "Unnamed user";
                    } else {
                        otherInfoPanel.baseName = base.getName();
                    }
                    otherInfoPanel.friend = true;
                    otherInfoPanel.bot = false;
                    break;
                case BOT:
                    otherInfoPanel.baseName = base.getName();
                    otherInfoPanel.bot = true;
                    break;
                case BOT_NCP:
                    otherInfoPanel.baseName = base.getName();
                    otherInfoPanel.friend = true;
                    otherInfoPanel.bot = true;
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown character: " + base.getCharacter());
            }
        } else if (otherSelection instanceof SyncResourceItemSimpleDto) {
            itemType = itemTypeService.getResourceItemType(otherSelection.getItemTypeId());
            otherInfoPanel.resource = true;
        } else if (otherSelection instanceof SyncBoxItemSimpleDto) {
            itemType = itemTypeService.getBoxItemType(otherSelection.getItemTypeId());
            otherInfoPanel.box = true;
        }
        if (itemType != null) {
            otherInfoPanel.imageUrl = CommonUrl.getImageServiceUrlSafe(itemType.getThumbnail());
            otherInfoPanel.itemTypeName = itemType.getName();
            otherInfoPanel.itemTypeDescr = itemType.getDescription();
        }
        return otherInfoPanel;
    }

    private BuildupItemCockpit[] createBuildupItemInfos(BaseItemType baseItemType, Group selectedGroup) {
        List<Integer> ableToBuildIds;
        Consumer<BaseItemType> onBuildCallback;
        if (baseItemType.getBuilderType() != null) {
            ableToBuildIds = baseItemType.getBuilderType().getAbleToBuildIds();
            onBuildCallback = (itemType) -> {
                BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().baseItemCount(1).baseItemTypeId(itemType.getId());
                baseItemPlacerService.activate(baseItemPlacerConfig, true, decimalPositions -> {
                    gameEngineControl.get().buildCmd(selectedGroup.getItems().stream().findFirst().orElseThrow(RuntimeException::new), CollectionUtils.getFirst(decimalPositions), itemType);
                });
            };
        } else if (baseItemType.getFactoryType() != null) {
            ableToBuildIds = baseItemType.getFactoryType().getAbleToBuildIds();
            // Factory
            onBuildCallback = (itemType) -> {
                selectedGroup.getSyncBaseItemsMonitors().stream()
                        .filter(syncBaseItemMonitor -> syncBaseItemMonitor.getConstructingBaseItemTypeId() == null)
                        .map(SyncItemMonitor::getSyncItemId)
                        .findFirst()
                        .ifPresent(factoryId -> gameEngineControl.get().fabricateCmd(factoryId, itemType));
            };
        } else {
            return null;
        }

        return ableToBuildIds.stream()
                .filter(itemTypeId -> gameUiControl.get().getPlanetConfig().imitation4ItemType(itemTypeId) > 0)
                .map(itemTypeId -> {
                    BaseItemType itemType = itemTypeService.getBaseItemType(itemTypeId);
                    BuildupItemCockpit buildupItemInfo = new BuildupItemCockpit() {
                        @Override
                        public void onBuild() {
                            try {
                                onBuildCallback.accept(itemType);
                            } catch (Throwable t) {
                                logger.log(Level.WARNING, t.getMessage(), t);
                            }
                        }

                        @Override
                        public void setAngularZoneRunner(AngularZoneRunner angularZoneRunner) {
                            this.angularZoneRunner = angularZoneRunner;
                        }

                        @Override
                        public void updateState() {
                            itemCount = baseItemUiService.get().getMyItemCount(itemType.getId());
                            itemLimit = gameUiControl.get().getMyLimitation4ItemType(itemType.getId());

                            if (baseItemUiService.get().isMyLevelLimitation4ItemTypeExceeded(itemType, 1)) {
                                buildLimitReached = true;
                                enabled = false;
                            } else if (baseItemUiService.get().isMyHouseSpaceExceeded(itemType, 1)) {
                                buildHouseSpaceReached = true;
                                enabled = false;
                            } else if (itemType.getPrice() > baseItemUiService.get().getResources()) {
                                buildNoMoney = true;
                                enabled = false;
                            } else {
                                enabled = true;

                            }
                        }

                        @Override
                        public void updateResources(int resources) {
                            if (itemType.getPrice() > resources && enabled) {
                                buildNoMoney = true;
                                enabled = false;
                            } else if (itemType.getPrice() <= resources && !enabled) {
                                updateState();
                            }
                        }
                    };
                    buildupItemInfo.imageUrl = CommonUrl.getImageServiceUrlSafe(itemType.getThumbnail());
                    buildupItemInfo.itemTypeId = itemType.getId();
                    buildupItemInfo.itemTypeName = itemType.getName();
                    buildupItemInfo.price = itemType.getPrice();
                    buildupItemInfo.updateState();
                    buildupItemCockpits.add(buildupItemInfo);
                    return buildupItemInfo;
                }).toArray(BuildupItemCockpit[]::new);
    }

    private ItemContainerCockpit createItemContainerInfo(BaseItemType baseItemType, Group selectedGroup) {
        if (baseItemType.getItemContainerType() != null) {
            SyncBaseItemMonitor transporter = selectedGroup.getSyncBaseItemsMonitors().stream().findFirst().orElseThrow(RuntimeException::new);
            ItemContainerCockpit itemContainerCockpit = new ItemContainerCockpit() {
                @Override
                public void setAngularZoneRunner(AngularZoneRunner angularZoneRunner) {
                    this.angularZoneRunner = angularZoneRunner;
                }

                @Override
                public void onUnload() {
                    try {
                        int baseItemTypeId = transporter.getSyncBaseItemState().getContainingItemTypeIds()[0];
                        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().baseItemCount(1).baseItemTypeId(baseItemTypeId);
                        baseItemPlacerService.activate(baseItemPlacerConfig, true, decimalPositions -> {
                            gameEngineControl.get().unloadContainerCmd(transporter.getSyncItemId(), CollectionUtils.getFirst(decimalPositions));
                        });
                    } catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            };
            transporter.setContainingChangeListener(syncItemMonitor -> {
                if (itemContainerCockpit.angularZoneRunner == null) {
                    logger.info("temContainerCockpit.angularZoneRunner == null in ItemCockpitService.createItemContainerInfo()");
                } else {
                    itemContainerCockpit.angularZoneRunner.runInAngularZone(() ->
                            // Do not replace by methode reference. GWT can not handle that.
                            itemContainerCockpit.count = transporter.getSyncBaseItemState().getContainingItemTypeIds() != null ? transporter.getSyncBaseItemState().getContainingItemTypeIds().length : 0
                    );
                }
            });

            itemContainerCockpit.count = transporter.getSyncBaseItemState().getContainingItemTypeIds() != null ? transporter.getSyncBaseItemState().getContainingItemTypeIds().length : 0;
            return itemContainerCockpit;
        } else {
            return null;
        }
    }

    private OwnItemCockpit createSimpleOwnItemCockpit(BaseItemType baseItemType, Collection<SyncBaseItemSimpleDto> items) {
        OwnItemCockpit ownInfoPanel = new OwnItemCockpit();
        ownInfoPanel.imageUrl = CommonUrl.getImageServiceUrlSafe(baseItemType.getThumbnail());
        ownInfoPanel.itemTypeName = baseItemType.getName();
        ownInfoPanel.itemTypeDescr = baseItemType.getDescription();
        if (!gameUiControl.get().isSellSuppressed()) {
            ownInfoPanel.sellHandler = () -> {
                gameEngineControl.get().sellItems(items);
            };
        }
        return ownInfoPanel;
    }

    // This method is may not called enough. Only called on Level change and houseSpace and usedHouseSpace changed
    // If an item is created  and item of a different item type is killed, this is method not called
    public void onStateChanged() {
        buildupItemCockpits.forEach(buildupItemCockpit -> {
                    if (buildupItemCockpit.angularZoneRunner == null) {
                        logger.info("buildupItemCockpit.angularZoneRunner == null in ItemCockpitService.onStateChanged()");
                    } else {
                        buildupItemCockpit.angularZoneRunner.runInAngularZone(() ->
                                // Do not replace by methode reference. GWT can not handle that.
                                buildupItemCockpit.updateState()
                        );
                    }
                }
        );
    }

    public void onResourcesChanged(int resources) {
        buildupItemCockpits.forEach(buildupItemCockpit -> {
            if (buildupItemCockpit.angularZoneRunner == null) {
                logger.info("buildupItemCockpit.angularZoneRunner == null in ItemCockpitService.onResourcesChanged()");
            } else {
                buildupItemCockpit.angularZoneRunner.runInAngularZone(() -> buildupItemCockpit.updateResources(resources));
            }
        });
    }
}
