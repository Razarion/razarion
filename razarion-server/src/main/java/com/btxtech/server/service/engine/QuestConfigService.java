package com.btxtech.server.service.engine;

import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import com.btxtech.server.repository.engine.QuestConfigRepository;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import org.springframework.stereotype.Service;

@Service
public class QuestConfigService extends AbstractConfigCrudPersistence<QuestConfig, QuestConfigEntity> {
    public QuestConfigService(QuestConfigRepository questConfigRepository) {
        super(QuestConfigEntity.class, questConfigRepository);
    }

    @Override
    protected QuestConfig toConfig(QuestConfigEntity entity) {
        return entity.toQuestConfig();
    }

    @Override
    protected void fromConfig(QuestConfig config, QuestConfigEntity entity) {
        throw new UnsupportedOperationException("...TODO...");
    }
}
