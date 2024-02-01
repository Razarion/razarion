package com.btxtech.uiservice.questvisualization;

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
import com.btxtech.uiservice.renderer.task.visualization.ItemVisualizationRenderTask;
import jsinterop.annotations.JsType;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
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
    @Inject
    private ItemVisualizationRenderTask itemVisualizationRenderTask;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private BoxUiService boxUiService;
    @Inject
    private Instance<QuestInGamePlaceVisualization> instanceQuestInGamePlaceVisualization;
    private AbstractSyncItemSetPositionMonitor syncItemSetPositionMonitor;
    private QuestInGamePlaceVisualization questInGamePlaceVisualization;
    private QuestConfig quest;
    private Set<Integer> itemTypeFilter;
    private boolean visible = true;
    private Consumer<Boolean> visibleCallback;
    private Consumer<Boolean> suppressCallback;
    private Collection<Runnable> placeConfigCallback = new ArrayList<>();

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
                setupVisualizationPlaceConfig(quest.getConditionConfig().getComparisonConfig().getPlaceConfig());
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
        markerConfig.outOfViewNodesMaterialId = gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig().getOutOfViewNodesMaterialId();
        markerConfig.outOfViewSize = gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig().getOutOfViewSize();
        markerConfig.outOfViewDistanceFromCamera = gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig().getOutOfViewDistanceFromCamera();
        return markerConfig;
    }

    private void setupVisualization(AbstractSyncItemSetPositionMonitor syncItemSetPositionMonitor) {
        this.syncItemSetPositionMonitor = syncItemSetPositionMonitor;
    }

    private void setupVisualizationPlaceConfig(PlaceConfig placeConfig) {
        if (placeConfig != null) {
            questInGamePlaceVisualization = instanceQuestInGamePlaceVisualization.get();
            questInGamePlaceVisualization.init(placeConfig, gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig());
            placeConfigCallback.forEach(Runnable::run);
            itemVisualizationRenderTask.activate(questInGamePlaceVisualization);
        }
    }

    private void hideVisualization() {
        if (questInGamePlaceVisualization != null) {
            questInGamePlaceVisualization = null;
            placeConfigCallback.forEach(Runnable::run);
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

    public boolean isQuestInGamePlaceVisualization() {
        return questInGamePlaceVisualization != null;
    }

    public QuestInGamePlaceVisualization getQuestInGamePlaceVisualization() {
        return questInGamePlaceVisualization;
    }

    public void addPlaceConfigCallback(Runnable runnable) {
        placeConfigCallback.add(runnable);
    }
}
