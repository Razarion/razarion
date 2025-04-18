package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.AudioLibraryEntity;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestConfigRepository extends JpaRepository<QuestConfigEntity, Integer> {
}
