package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.quest.QuestBackendInfo;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import com.btxtech.server.repository.engine.QuestConfigRepository;
import com.btxtech.server.repository.engine.ServerGameEngineConfigRepository;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

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

    @Transactional
    public List<QuestBackendInfo> readQuestBackendInfos() {
        return readAllBaseEntities()
                .stream()
                .map(
                        questConfigEntity -> new QuestBackendInfo()
                                .id(questConfigEntity.getId())
                                .conditionConfig(questConfigEntity.toQuestConfig().getConditionConfig())
                                .levelNumber(serverGameEngineConfigRepository.findMinimalLevelNumberByQuestConfigId(questConfigEntity.getId()).orElse(-1)))
                .sorted(Comparator.comparingInt(QuestBackendInfo::getLevelNumber))
                .toList();
    }

}
