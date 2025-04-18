package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.Rectangle2D;
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
import jsinterop.annotations.JsType;

import javax.inject.Provider;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;


/**
 * Created by Beat
 * on 12.09.2017.
 */
@JsType
@Singleton
public class InGameQuestVisualizationService {

    // private Logger logger = Logger.getLogger(InGameQuestVisualizationService.class.getName());
    private GameUiControl gameUiControl;

    private BaseItemUiService baseItemUiService;

    private ResourceUiService resourceUiService;

    private BoxUiService boxUiService;

    private Provider<QuestInGamePlaceVisualization> instanceQuestInGamePlaceVisualization;
    private AbstractSyncItemSetPositionMonitor syncItemSetPositionMonitor;
    private QuestInGamePlaceVisualization questInGamePlaceVisualization;
    private QuestConfig quest;
    private Set<Integer> itemTypeFilter;
    private boolean visible = true;
    private Consumer<Boolean> visibleCallback;
    private Consumer<Boolean> suppressCallback;
    private ViewField viewField;
    private Rectangle2D viewFieldAabb;

    @Inject
    public InGameQuestVisualizationService(Provider<com.btxtech.uiservice.questvisualization.QuestInGamePlaceVisualization> instanceQuestInGamePlaceVisualization, BoxUiService boxUiService, ResourceUiService resourceUiService, BaseItemUiService baseItemUiService, GameUiControl gameUiControl) {
        this.instanceQuestInGamePlaceVisualization = instanceQuestInGamePlaceVisualization;
        this.boxUiService = boxUiService;
        this.resourceUiService = resourceUiService;
        this.baseItemUiService = baseItemUiService;
        this.gameUiControl = gameUiControl;
    }

    public void onQuestActivated(QuestConfig quest) {
        stop();
        this.quest = quest;
        if (visibleCallback != null) {
            visibleCallback.accept(true);
        }
        visible = true;
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
                if(syncItemSetPositionMonitor != null) {
                    ((SyncBaseItemSetPositionMonitor)syncItemSetPositionMonitor).setItemTypeFilter(itemTypeFilter);
                }
            }
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
            showVisualization();
        } else {
            hideVisualization();
        }
        if (suppressCallback != null) {
            visibleCallback.accept(visible);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void stop() {
        hideVisualization();
        quest = null;
        itemTypeFilter = null;
    }

    private void showVisualization() {
        if (quest == null) {
            return;
        }
        switch (quest.getConditionConfig().getConditionTrigger()) {
            case SYNC_ITEM_KILLED: {
                if (itemTypeFilter == null) {
                    if (quest.getConditionConfig().getComparisonConfig().getTypeCount() != null) {
                        itemTypeFilter = quest.getConditionConfig().getComparisonConfig().getTypeCount().keySet();
                    }
                }
                MarkerConfig markerConfig = setupMarkerConfig();
                setupVisualization(baseItemUiService.createSyncItemSetPositionMonitor(markerConfig, itemTypeFilter, quest.getConditionConfig().getComparisonConfig().toBotIdSet()));
                break;
            }
            case HARVEST: {
                MarkerConfig markerConfig = setupMarkerConfig();
                setupVisualization(resourceUiService.createSyncItemSetPositionMonitor(markerConfig));
                break;
            }
            case SYNC_ITEM_POSITION: {
                MarkerConfig markerConfig = setupMarkerConfig();
                setupVisualizationPlaceConfig(quest.getConditionConfig().getComparisonConfig().getPlaceConfig(), markerConfig);
                if (viewField != null && viewFieldAabb != null && questInGamePlaceVisualization != null) {
                    questInGamePlaceVisualization.onViewChanged(viewField, viewFieldAabb);
                }
                break;
            }
            case BOX_PICKED: {
                MarkerConfig markerConfig = setupMarkerConfig();
                setupVisualization(boxUiService.createSyncItemSetPositionMonitor(markerConfig));
                break;
            }
        }
    }

    private MarkerConfig setupMarkerConfig() {
        MarkerConfig markerConfig = new MarkerConfig();
        markerConfig.radius = gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig().getRadius();
        markerConfig.nodesMaterialId = gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig().getNodesMaterialId();
        markerConfig.placeNodesMaterialId = gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig().getPlaceNodesMaterialId();
        markerConfig.outOfViewNodesMaterialId = gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig().getOutOfViewNodesMaterialId();
        markerConfig.outOfViewSize = gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig().getOutOfViewSize();
        markerConfig.outOfViewDistanceFromCamera = gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig().getOutOfViewDistanceFromCamera();
        return markerConfig;
    }

    private void setupVisualization(AbstractSyncItemSetPositionMonitor syncItemSetPositionMonitor) {
        this.syncItemSetPositionMonitor = syncItemSetPositionMonitor;
    }

    private void setupVisualizationPlaceConfig(PlaceConfig placeConfig, MarkerConfig markerConfig) {
        if (placeConfig != null) {
            questInGamePlaceVisualization = instanceQuestInGamePlaceVisualization.get();
            questInGamePlaceVisualization.init(placeConfig, markerConfig);
        }
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

    public void setVisibleCallback(Consumer<Boolean> visibleCallback) {
        this.visibleCallback = visibleCallback;
    }

    public void setSuppressCallback(Consumer<Boolean> suppressCallback) {
        this.suppressCallback = suppressCallback;
    }

    public void setSuppressed(boolean suppress) {
        if (visibleCallback != null) {
            visibleCallback.accept(!suppress);
        }
        if (suppressCallback != null) {
            suppressCallback.accept(suppress);
        }
        visible = !suppress;
        if (suppress) {
            hideVisualization();
        } else {
            showVisualization();
        }
    }

    public void onViewChanged(ViewField viewField, Rectangle2D viewFieldAabb) {
        this.viewField = viewField;
        this.viewFieldAabb = viewFieldAabb;
        if (questInGamePlaceVisualization != null) {
            questInGamePlaceVisualization.onViewChanged(viewField, viewFieldAabb);
        }
    }
}
