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
            SELECT e.quest
            FROM ServerGameEngineConfigEntity sg
            JOIN sg.serverLevelQuestEntities s
            JOIN s.serverLevelQuestEntryEntities e
            WHERE sg.id = :serverGameEngineConfigEntityId
              AND s.minimalLevel.number <= :levelNumber
              AND (:ignoreQuestIds IS NULL OR e.quest.id NOT IN :ignoreQuestIds)
            ORDER BY s.minimalLevel.number, e.orderColumn
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

    /**
     * Where every quest sits: which level it belongs to, and where in that level it stands.
     * <p>
     * One row per quest rather than one query per quest - the caller wants all of them, and asking
     * for the level one at a time is a hundred round trips on a pod with one connection pool.
     * <p>
     * {@code entry.orderColumn} is the order the game itself offers the quests in; it is what
     * {@link ServerLevelQuestEntity} sorts its entries by. Without it the only orders available to
     * a reader are the quest id, which is not the order they are played in, and how often each was
     * passed, which says nothing at all once two of them are equal.
     *
     * @return {questConfigId, levelNumber, orderColumn} per row
     */
    @Query("""
                SELECT entry.quest.id, slqe.minimalLevel.number, entry.orderColumn
                FROM ServerLevelQuestEntity slqe
                JOIN slqe.serverLevelQuestEntryEntities entry
            """)
    List<Object[]> findQuestLevelAndOrder();
}
