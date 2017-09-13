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

    public void onQuestActivated(QuestConfig quest) {
        stop();
        this.quest = quest;
        switch (quest.getConditionConfig().getConditionTrigger()) {
            case SYNC_ITEM_KILLED:
                Set<Integer> itemTypeFilter = null;
                if (quest.getConditionConfig().getComparisonConfig().getTypeCount() != null) {
                    itemTypeFilter = quest.getConditionConfig().getComparisonConfig().getTypeCount().keySet();
                }
                startVisualization(gameUiControl.getColdGameUiControlConfig().getInGameQuestVisualConfig().getAttackColor(), baseItemUiService.createSyncItemSetPositionMonitor(itemTypeFilter));
                break;
            case HARVEST:
                startVisualization(gameUiControl.getColdGameUiControlConfig().getInGameQuestVisualConfig().getHarvestColor(), resourceUiService.createSyncItemSetPositionMonitor());
                break;
            case SYNC_ITEM_POSITION:
                // TODO
                break;
            case BOX_PICKED:
                break;
        }
    }

    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        if (quest.getConditionConfig().getConditionTrigger() == ConditionTrigger.SYNC_ITEM_KILLED) {
            if (questProgressInfo.getTypeCount() != null) {
                Set<Integer> itemTypeFilter = new HashSet<>();
                for (Map.Entry<Integer, Integer> entry : quest.getConditionConfig().getComparisonConfig().getTypeCount().entrySet()) {
                    int actual = questProgressInfo.getTypeCount().get(entry.getKey());
                    if (actual < entry.getValue()) {
                        itemTypeFilter.add(entry.getKey());
                    }
                }
                ((SyncBaseItemSetPositionMonitor) questInGameItemVisualization.getSyncItemSetPositionMonitor()).setItemTypeFilter(itemTypeFilter);
            }
        }
    }

    public void stop() {
        if (questInGameItemVisualization != null) {
            questInGameItemVisualization.releaseMonitor();
            itemVisualizationRenderTask.deactivate();
            questInGameItemVisualization = null;
        }
        quest = null;
    }

    private void startVisualization(Color color, AbstractSyncItemSetPositionMonitor syncItemSetPositionMonitor) {
        InGameQuestVisualConfig inGameQuestVisualConfig = gameUiControl.getColdGameUiControlConfig().getInGameQuestVisualConfig();
        questInGameItemVisualization = instance.get();
        questInGameItemVisualization.init(color, inGameQuestVisualConfig, syncItemSetPositionMonitor);
        itemVisualizationRenderTask.activate(questInGameItemVisualization);
    }
}
