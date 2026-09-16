package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.quest.QuestBackendInfo;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import com.btxtech.server.repository.engine.QuestConfigRepository;
import com.btxtech.server.repository.engine.ServerGameEngineConfigRepository;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestConfigService extends AbstractConfigCrudService<QuestConfig, QuestConfigEntity> {
    private final ServerGameEngineConfigRepository serverGameEngineConfigRepository;

    public QuestConfigService(QuestConfigRepository questConfigRepository,
                              ServerGameEngineConfigRepository serverGameEngineConfigRepository) {
        super(QuestConfigEntity.class, questConfigRepository);
        this.serverGameEngineConfigRepository = serverGameEngineConfigRepository;
    }

    @Override
    protected QuestConfig toConfig(QuestConfigEntity entity) {
        return entity.toQuestConfig();
    }

    @Override
    protected void fromConfig(QuestConfig config, QuestConfigEntity entity) {
        throw new UnsupportedOperationException("...TODO...");
    }

    /**
     * Every quest with the level it belongs to and where it stands inside that level.
     * <p>
     * The placement is read in one query rather than one per quest: this used to ask the database
     * for a quest's level once per quest, which is a hundred round trips for a list that is wanted
     * whole.
     * <p>
     * Sorted the way the game runs them - by level, then by the level's own order - so a reader
     * gets the sequence rather than the order the rows happened to come back in. Quests belonging
     * to no level keep -1 and sort to the front, where they are visible rather than scattered.
     */
    @Transactional
    public List<QuestBackendInfo> readQuestBackendInfos() {
        Map<Integer, int[]> placements = new HashMap<>();
        for (Object[] row : serverGameEngineConfigRepository.findQuestLevelAndOrder()) {
            Integer questId = (Integer) row[0];
            Integer levelNumber = (Integer) row[1];
            Integer orderColumn = (Integer) row[2];
            placements.put(questId, new int[]{
                    levelNumber != null ? levelNumber : -1,
                    orderColumn != null ? orderColumn : -1});
        }
        return readAllBaseEntities()
                .stream()
                .map(questConfigEntity -> {
                    int[] placement = placements.getOrDefault(questConfigEntity.getId(), new int[]{-1, -1});
                    return new QuestBackendInfo()
                            .id(questConfigEntity.getId())
                            .conditionConfig(questConfigEntity.toQuestConfig().getConditionConfig())
                            .levelNumber(placement[0])
                            .orderInLevel(placement[1]);
                })
                .sorted(Comparator.comparingInt(QuestBackendInfo::getLevelNumber)
                        .thenComparingInt(QuestBackendInfo::getOrderInLevel)
                        .thenComparingInt(QuestBackendInfo::getId))
                .toList();
    }

}
