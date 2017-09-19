package com.btxtech.uiservice.inventory;

import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.tip.GameTipService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 30.10.2016.
 */
public abstract class InventoryUiService {
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private GameTipService gameTipService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private ModalDialogManager dialogManager;
    private List<Integer> inventoryItemIds = new ArrayList<>();
    private List<Integer> inventoryArtifactIds = new ArrayList<>();

    protected abstract void loadServerInventoryInfo(Consumer<InventoryInfo> inventoryInfoConsumer);

    public void provideInventoryInfo(Consumer<InventoryInfo> inventoryInfoConsumer) {
        if (gameUiControl.getGameEngineMode() == GameEngineMode.SLAVE) {
            loadServerInventoryInfo(inventoryInfoConsumer);
        } else if (gameUiControl.getGameEngineMode() == GameEngineMode.MASTER) {
            inventoryInfoConsumer.accept(new InventoryInfo().setCrystals(0).setInventoryItemIds(inventoryItemIds).setInventoryArtifactIds(inventoryArtifactIds));
        } else {
            throw new IllegalArgumentException("InventoryUiService.gatherInventoryItemModels(): Unknown GameEngineMode: " + gameUiControl.getGameEngineMode());
        }
    }

    public void onOnBoxPicked(BoxContent boxContent) {
        if (gameUiControl.getGameEngineMode() == GameEngineMode.MASTER) {
            for (InventoryItem inventoryItem : boxContent.getInventoryItems()) {
                inventoryItemIds.add(inventoryItem.getId());
            }
        }
        dialogManager.showBoxPicked(boxContent);
    }

    public void useItem(InventoryItem inventoryItem) {
        if (inventoryItem.getRazarion() != null && inventoryItem.getRazarion() > 0) {
            gameEngineControl.useInventoryItem(new UseInventoryItem().setInventoryId(inventoryItem.getId()));
            if (gameUiControl.getGameEngineMode() == GameEngineMode.MASTER) {
                inventoryItemIds.remove((Integer)inventoryItem.getId());
            }
        } else if (inventoryItem.hasBaseItemTypeId()) {
            try {
                BaseItemType baseItemType = itemTypeService.getBaseItemType(inventoryItem.getBaseItemTypeId());
                if (baseItemUiService.isMyLevelLimitation4ItemTypeExceeded(baseItemType, inventoryItem.getBaseItemTypeCount())) {
                    modalDialogManager.showUseInventoryItemLimitExceeded(baseItemType);
                } else if (baseItemUiService.isMyHouseSpaceExceeded(baseItemType, inventoryItem.getBaseItemTypeCount())) {
                    modalDialogManager.showUseInventoryHouseSpaceExceeded();
                } else {
                    BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig();
                    baseItemPlacerConfig.setBaseItemTypeId(baseItemType.getId());
                    baseItemPlacerConfig.setBaseItemCount(inventoryItem.getBaseItemTypeCount());
                    baseItemPlacerConfig.setEnemyFreeRadius(inventoryItem.getBaseItemTypeFreeRange());
                    baseItemPlacerService.activate(baseItemPlacerConfig, decimalPositions -> {
                        gameEngineControl.useInventoryItem(new UseInventoryItem().setInventoryId(inventoryItem.getId()).setPositions(new ArrayList<>(decimalPositions)));
                        if (gameUiControl.getGameEngineMode() == GameEngineMode.MASTER) {
                            inventoryItemIds.remove((Integer)inventoryItem.getId());
                        }
                    });
                    gameTipService.onInventoryItemPlacerActivated(inventoryItem);
                }
            } catch (Throwable e) {
                exceptionHandler.handleException("InventoryUiService.useItem()", e);
            }
        }
    }
}
