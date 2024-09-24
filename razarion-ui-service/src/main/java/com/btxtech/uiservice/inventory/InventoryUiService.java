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
import jsinterop.annotations.JsType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 30.10.2016.
 */
@JsType
public class InventoryUiService {

    private ItemTypeService itemTypeService;

    private GameUiControl gameUiControl;

    private ExceptionHandler exceptionHandler;

    private ModalDialogManager modalDialogManager;

    private BaseItemPlacerService baseItemPlacerService;

    private BaseItemUiService baseItemUiService;

    private GameEngineControl gameEngineControl;

    private ModalDialogManager dialogManager;
    private List<Integer> inventoryItemIds = new ArrayList<>();
    private List<Integer> inventoryArtifactIds = new ArrayList<>();

    @Inject
    public InventoryUiService(ModalDialogManager dialogManager, GameEngineControl gameEngineControl, BaseItemUiService baseItemUiService, BaseItemPlacerService baseItemPlacerService, ModalDialogManager modalDialogManager, ExceptionHandler exceptionHandler, GameUiControl gameUiControl, ItemTypeService itemTypeService) {
        this.dialogManager = dialogManager;
        this.gameEngineControl = gameEngineControl;
        this.baseItemUiService = baseItemUiService;
        this.baseItemPlacerService = baseItemPlacerService;
        this.modalDialogManager = modalDialogManager;
        this.exceptionHandler = exceptionHandler;
        this.gameUiControl = gameUiControl;
        this.itemTypeService = itemTypeService;
    }

    @Deprecated // Done in angular
    public void provideInventoryInfo(Consumer<InventoryInfo> inventoryInfoConsumer) {
        if (gameUiControl.getGameEngineMode() == GameEngineMode.SLAVE) {
            // TODO loadServerInventoryInfo(inventoryInfoConsumer);
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

    @SuppressWarnings("unused") // Called by Angular
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
                    baseItemPlacerService.activate(baseItemPlacerConfig, true, decimalPositions -> {
                        gameEngineControl.useInventoryItem(new UseInventoryItem().setInventoryId(inventoryItem.getId()).setPositions(new ArrayList<>(decimalPositions)));
                        if (gameUiControl.getGameEngineMode() == GameEngineMode.MASTER) {
                            inventoryItemIds.remove((Integer)inventoryItem.getId());
                        }
                    });
                    // TODO gameTipService.onInventoryItemPlacerActivated(inventoryItem);
                }
            } catch (Throwable e) {
                exceptionHandler.handleException("InventoryUiService.useItem()", e);
            }
        }
    }
}
