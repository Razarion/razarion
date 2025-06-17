package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.ServerGameEngineConfigEntity;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServerGameEngineConfigRepository extends JpaRepository<ServerGameEngineConfigEntity, Integer> {

    @Query("""
            SELECT q
            FROM QuestConfigEntity q
            WHERE q.id IN (
                SELECT e.quest.id
                FROM ServerGameEngineConfigEntity sg
                JOIN sg.serverLevelQuestEntities s
                JOIN s.serverLevelQuestEntryEntities e
                WHERE sg.id = :serverGameEngineConfigEntityId
                  AND s.minimalLevel.number <= :levelNumber
            )
            AND (:ignoreQuestIds IS NULL OR q.id NOT IN :ignoreQuestIds)
            """)
    List<QuestConfigEntity> getQuests4Level(@Param("levelNumber") int levelNumber,
                                            @Param("serverGameEngineConfigEntityId") int serverGameEngineConfigEntityId,
                                            @Param("ignoreQuestIds") Collection<Integer> ignoreQuestIds);


    @Query("""
                SELECT slqe.minimalLevel.number
                FROM ServerLevelQuestEntity slqe
                JOIN slqe.serverLevelQuestEntryEntities entry
                WHERE entry.quest.id = :questConfigId
            """)
    Optional<Integer> findMinimalLevelNumberByQuestConfigId(@Param("questConfigId") int questConfigId);
}
