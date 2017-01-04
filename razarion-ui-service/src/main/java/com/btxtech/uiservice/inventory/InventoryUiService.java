package com.btxtech.uiservice.inventory;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.InventoryService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.ModalDialogManager;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.itemplacer.BaseItemPlacerService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.tip.GameTipService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 30.10.2016.
 */
@ApplicationScoped
public class InventoryUiService {
    @Inject
    private InventoryService inventoryService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private GameUiControl gameUiControl;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private BaseItemPlacerService baseItemPlacerService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private GameTipService gameTipService;

    public List<InventoryItemModel> gatherInventoryItemModels(UserContext userContext) {
        Map<Integer, InventoryItemModel> inventoryItemModels = new HashMap<>();
        for (Integer inventoryItemId : userContext.getInventoryItemIds()) {
            InventoryItemModel model = inventoryItemModels.computeIfAbsent(inventoryItemId, k -> new InventoryItemModel(inventoryService.getInventoryItem(inventoryItemId)));
            model.increaseItemCount();
        }
        return new ArrayList<>(inventoryItemModels.values());
    }


    public void useItem(InventoryItem inventoryItem) {
        if (inventoryItem.hasBaseItemTypeId()) {
            try {
                BaseItemType baseItemType = itemTypeService.getBaseItemType(inventoryItem.getBaseItemType());
                if (gameUiControl.isLevelLimitation4ItemTypeExceeded(baseItemType, inventoryItem.getBaseItemTypeCount())) {
                    modalDialogManager.showUseInventoryItemLimitExceeded(baseItemType);
                } else if (gameUiControl.isHouseSpaceExceeded(baseItemType, inventoryItem.getBaseItemTypeCount())) {
                    modalDialogManager.showUseInventoryHouseSpaceExceeded();
                } else {
                    BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig();
                    baseItemPlacerConfig.setBaseItemTypeId(baseItemType.getId());
                    baseItemPlacerConfig.setBaseItemCount(inventoryItem.getBaseItemTypeCount());
                    baseItemPlacerConfig.setEnemyFreeRadius(inventoryItem.getItemFreeRange());
                    baseItemPlacerService.activate(baseItemPlacerConfig, decimalPositions -> {
                        try {
                            for (DecimalPosition position : decimalPositions) {
                                baseItemService.spawnSyncBaseItem(baseItemType, position, baseItemService.getPlayerBase(gameUiControl.getUserContext()), false);
                            }
                            gameLogicService.onInventoryItemPlaced(gameUiControl.getUserContext(), inventoryItem);
                        } catch (Exception e) {
                            exceptionHandler.handleException(e);
                        }
                    });
                    gameTipService.onInventoryItemPlacerActivated(inventoryItem);
                }
            } catch (Throwable e) {
                exceptionHandler.handleException("InventoryUiService.useItem()", e);
            }
        } else {
            throw new UnsupportedOperationException();
//            if (ClientBase.getInstance().isDepositResourceAllowed(inventoryItem.getGoldAmount())) {
//                Connection.getInstance().useInventoryItem(inventoryItem.getInventoryItemId(), null);
//            } else {
//                modalDialogManager.showMessageDialog(ClientI18nHelper.getConstants().useItem(), ClientI18nHelper.getConstants().useItemMoney()), DialogManager.Type.STACK_ABLE);
//            }
        }
    }
}
