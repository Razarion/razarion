package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.dto.InGameQuestVisualConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.AbstractSyncItemSetPositionMonitor;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.item.SyncBaseItemSetPositionMonitor;
import com.btxtech.uiservice.renderer.task.visualization.ItemVisualizationRenderTask;

import javax.enterprise.inject.Instance;
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
    private Instance<QuestInGameItemVisualization> instance;
    private QuestInGameItemVisualization questInGameItemVisualization;
    private QuestConfig quest;
    private Set<Integer> itemTypeFilter;
    private boolean visible = true;
    private Consumer<Boolean> visibleCallback;
    private Consumer<Boolean> suppressCallback;

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
        if(quest == null) {
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
                if (visible) {
                    ((SyncBaseItemSetPositionMonitor) questInGameItemVisualization.getSyncItemSetPositionMonitor()).setItemTypeFilter(itemTypeFilter);
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
        visibleCallback.accept(visible);
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
        if(quest == null) {
            return;
        }
        switch (quest.getConditionConfig().getConditionTrigger()) {
            case SYNC_ITEM_KILLED:
                if (itemTypeFilter == null) {
                    if (quest.getConditionConfig().getComparisonConfig().getTypeCount() != null) {
                        itemTypeFilter = quest.getConditionConfig().getComparisonConfig().getTypeCount().keySet();
                    }
                }
                setupVisualization(gameUiControl.getColdGameUiControlConfig().getInGameQuestVisualConfig().getAttackColor(), baseItemUiService.createSyncItemSetPositionMonitor(itemTypeFilter));
                break;
            case HARVEST:
                setupVisualization(gameUiControl.getColdGameUiControlConfig().getInGameQuestVisualConfig().getHarvestColor(), resourceUiService.createSyncItemSetPositionMonitor());
                break;
            case SYNC_ITEM_POSITION:
                // TODO
                break;
            case BOX_PICKED:
                break;
        }
    }

    private void setupVisualization(Color color, AbstractSyncItemSetPositionMonitor syncItemSetPositionMonitor) {
        InGameQuestVisualConfig inGameQuestVisualConfig = gameUiControl.getColdGameUiControlConfig().getInGameQuestVisualConfig();
        questInGameItemVisualization = instance.get();
        questInGameItemVisualization.init(color, inGameQuestVisualConfig, syncItemSetPositionMonitor);
        itemVisualizationRenderTask.activate(questInGameItemVisualization);
    }

    private void hideVisualization() {
        if (questInGameItemVisualization != null) {
            questInGameItemVisualization.releaseMonitor();
            itemVisualizationRenderTask.deactivate();
            questInGameItemVisualization = null;
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
}
