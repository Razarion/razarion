package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.InGameQuestVisualConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.AbstractSyncItemSetPositionMonitor;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.item.SyncBaseItemSetPositionMonitor;
import com.btxtech.uiservice.renderer.MarkerConfig;
import com.btxtech.uiservice.renderer.ViewField;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Singleton
public class InGameQuestVisualizationService {
    private final GameUiControl gameUiControl;
    private final BaseItemUiService baseItemUiService;
    private final ResourceUiService resourceUiService;
    private final BoxUiService boxUiService;
    private final Provider<QuestInGamePlaceVisualization> instanceQuestInGamePlaceVisualization;
    private AbstractSyncItemSetPositionMonitor syncItemSetPositionMonitor;
    private QuestInGamePlaceVisualization questInGamePlaceVisualization;
    private QuestConfig quest;
    private Set<Integer> itemTypeFilter;
    private ViewField viewField;
    private Rectangle2D viewFieldAabb;

    @Inject
    public InGameQuestVisualizationService(Provider<QuestInGamePlaceVisualization> instanceQuestInGamePlaceVisualization, BoxUiService boxUiService, ResourceUiService resourceUiService, BaseItemUiService baseItemUiService, GameUiControl gameUiControl) {
        this.instanceQuestInGamePlaceVisualization = instanceQuestInGamePlaceVisualization;
        this.boxUiService = boxUiService;
        this.resourceUiService = resourceUiService;
        this.baseItemUiService = baseItemUiService;
        this.gameUiControl = gameUiControl;
    }

    public void onQuestActivated(QuestConfig quest) {
        stop();
        this.quest = quest;
        showVisualization();
    }

    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        itemTypeFilter = null;
        if (quest == null) {
            return;
        }
        if (quest.getConditionConfig().getConditionTrigger() == ConditionTrigger.SYNC_ITEM_KILLED) {
            if (questProgressInfo.getTypeCount() != null) {
                itemTypeFilter = new HashSet<>();
                for (Map.Entry<Integer, Integer> entry : quest.getConditionConfig().getComparisonConfig().getTypeCount().entrySet()) {
                    int actual = questProgressInfo.getTypeCount().get(entry.getKey());
                    if (actual < entry.getValue()) {
                        itemTypeFilter.add(entry.getKey());
                    }
                }
                if (syncItemSetPositionMonitor != null) {
                    ((SyncBaseItemSetPositionMonitor) syncItemSetPositionMonitor).setItemTypeFilter(itemTypeFilter);
                }
            }
        }
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setVisible(boolean visible) {
        if (visible) {
            showVisualization();
        } else {
            hideVisualization();
        }
    }

    public void stop() {
        hideVisualization();
        quest = null;
        itemTypeFilter = null;
    }

    public void onViewChanged(ViewField viewField, Rectangle2D viewFieldAabb) {
        this.viewField = viewField;
        this.viewFieldAabb = viewFieldAabb;
        if (questInGamePlaceVisualization != null) {
            questInGamePlaceVisualization.onViewChanged(viewField, viewFieldAabb);
        }
    }

    private void showVisualization() {
        if (quest == null || quest.getTipConfig() != null) {
            return;
        }
        switch (quest.getConditionConfig().getConditionTrigger()) {
            case SYNC_ITEM_KILLED: {
                if (itemTypeFilter == null && quest.getConditionConfig().getComparisonConfig().getTypeCount() != null) {
                    itemTypeFilter = quest.getConditionConfig().getComparisonConfig().getTypeCount().keySet();
                }
                MarkerConfig markerConfig = setupMarkerConfig();
                setupVisualization(baseItemUiService.createSyncItemSetPositionMonitor(markerConfig, itemTypeFilter, quest.getConditionConfig().getComparisonConfig().toBotIdSet()));
                break;
            }
            case HARVEST: {
                setupVisualization(resourceUiService.createSyncItemSetPositionMonitor(setupMarkerConfig()));
                break;
            }
            case SYNC_ITEM_POSITION: {
                PlaceConfig placeConfig = quest.getConditionConfig().getComparisonConfig().getPlaceConfig();
                if (placeConfig != null) {
                    questInGamePlaceVisualization = instanceQuestInGamePlaceVisualization.get();
                    questInGamePlaceVisualization.init(placeConfig, setupMarkerConfig());
                    if (viewField != null && viewFieldAabb != null) {
                        questInGamePlaceVisualization.onViewChanged(viewField, viewFieldAabb);
                    }
                }
                break;
            }
            case BOX_PICKED: {
                setupVisualization(boxUiService.createSyncItemSetPositionMonitor(setupMarkerConfig()));
                break;
            }
        }
    }

    private MarkerConfig setupMarkerConfig() {
        MarkerConfig markerConfig = new MarkerConfig();
        InGameQuestVisualConfig config = gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig();
        markerConfig.radius = config.getRadius();
        markerConfig.nodesMaterialId = config.getNodesMaterialId();
        markerConfig.placeNodesMaterialId = config.getPlaceNodesMaterialId();
        markerConfig.outOfViewNodesMaterialId = config.getOutOfViewNodesMaterialId();
        markerConfig.outOfViewSize = config.getOutOfViewSize();
        markerConfig.outOfViewDistanceFromCamera = config.getOutOfViewDistanceFromCamera();
        return markerConfig;
    }

    private void setupVisualization(AbstractSyncItemSetPositionMonitor syncItemSetPositionMonitor) {
        this.syncItemSetPositionMonitor = syncItemSetPositionMonitor;
    }

    private void hideVisualization() {
        if (questInGamePlaceVisualization != null) {
            questInGamePlaceVisualization.release();
            questInGamePlaceVisualization = null;
        }
        if (syncItemSetPositionMonitor != null) {
            syncItemSetPositionMonitor.release();
            syncItemSetPositionMonitor = null;
        }
    }
}
