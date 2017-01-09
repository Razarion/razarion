package com.btxtech.uiservice.tip.tiptask;


import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.Group;
import com.btxtech.shared.dto.GameTipVisualConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.QuestVisualizer;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.tip.GameTipService;
import com.btxtech.uiservice.tip.visualization.AbstractGuiTipVisualization;
import com.btxtech.uiservice.tip.visualization.InGameDirectionVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

/**
 * User: beat
 * Date: 22.08.12
 * Time: 13:19
 */
public abstract class AbstractTipTask {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SelectionHandler selectionHandler;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private QuestVisualizer questVisualizer;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private BaseItemUiService baseItemUiService;
    private GameTipService gameTipService;
    private boolean failOnSelectionCleared;
    private boolean failOnTargetSelectionChanged;

    void setGameTipService(GameTipService gameTipService) {
        this.gameTipService = gameTipService;
    }

    // Override ins subclasses
    public InGameTipVisualization createInGameTipVisualization() {
        return null;
    }

    // Override ins subclasses
    public InGameDirectionVisualization createInGameDirectionVisualization() {
        return null;
    }

    // Override ins subclasses
    public AbstractGuiTipVisualization createGuiTipVisualization() {
        return null;
    }

    public abstract boolean isFulfilled();

    protected abstract void internalStart();

    protected abstract void internalCleanup();

    // Override ins subclasses
    protected void onOwnSelectionChanged(Group selectedGroup) {
    }

    // Override ins subclasses
    protected void onCommandSent(CommandInfo commandInfo) {
    }

    // Override ins subclasses
    protected void onSyncBaseItemIdle(SyncBaseItemSimpleDto syncBaseItem) {
    }

    // Override ins subclasses
    protected void onSpawnSyncItem(SyncBaseItemSimpleDto syncBaseItem) {
    }

    // Override ins subclasses
    protected void onInventoryDialogOpened() {
    }

    // Override ins subclasses
    protected void onInventoryDialogClosed() {
    }

    // Override ins subclasses
    protected void onInventoryItemPlacerActivated(InventoryItem inventoryItem) {
    }

    void onFailed() {
        cleanup();
        gameTipService.onTaskFailed();
    }

    protected void onSucceed() {
        cleanup();
        gameTipService.onSucceed();
    }

    public void start() {
        internalStart();
    }

    public void cleanup() {
        internalCleanup();
    }

    final void onSelectionChanged(SelectionEvent selectionEvent) {
        switch (selectionEvent.getType()) {
            case CLEAR: {
                if (failOnSelectionCleared) {
                    onFailed();
                }
                break;
            }
            case OWN: {
                onOwnSelectionChanged(selectionEvent.getSelectedGroup());
                break;
            }
            case OTHER: {
                if (failOnTargetSelectionChanged) {
                    onFailed();
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown SelectionEvent.Type: " + selectionEvent.getType());
        }
    }

    void activateFailOnSelectionCleared() {
        failOnSelectionCleared = true;
    }

    void activateFailOnTargetSelectionChanged() {
        failOnTargetSelectionChanged = true;
    }

    private Group getOwnSelection() {
        return selectionHandler.getOwnSelection();
    }

    boolean isSingleSelection(int selectedItemTypeId, Group selectedGroup) {
        Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> selectedItemTypes = selectedGroup.getGroupedItems();
        return selectedItemTypes.size() == 1 && CollectionUtils.getFirst(selectedItemTypes.keySet()).getId() == selectedItemTypeId;
    }

    boolean isSingleSelection(int selectedItemTypeId) {
        Group selectedGroup = getOwnSelection();
        return selectedGroup != null && isSingleSelection(selectedItemTypeId, selectedGroup);
    }

    void setShowInGameQuestVisualisation(boolean show) {
        questVisualizer.setShowInGameVisualisation(show);
    }

    GameTipVisualConfig getGameTipVisualConfig() {
        return gameUiControl.getGameUiControlConfig().getGameTipVisualConfig();
    }

    Collection<SyncBaseItemSimpleDto> findItemsOfType(int baseItemTypeId) {
        return baseItemUiService.findMyItemsOfType(baseItemTypeId);
    }
}
