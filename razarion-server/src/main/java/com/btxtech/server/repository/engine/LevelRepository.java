package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.LevelUnlockEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface LevelRepository extends JpaRepository<LevelEntity, Integer> {
    LevelEntity findTopByOrderByNumberAsc();

    @Query("SELECT l.number FROM LevelEntity l WHERE l.id = :levelId")
    Integer getLevelNumberByLevelId(@Param("levelId") int levelId);

    @Query("""
            SELECT unlock
            FROM LevelEntity level
            JOIN level.levelUnlockEntities unlock
            WHERE level.number <= :levelNumber
            AND (:unlockedEntityIds IS NULL OR unlock.id NOT IN :unlockedEntityIds)
            """)
    List<LevelUnlockEntity> findLockedUnlocks(@Param("levelNumber") int levelNumber,
                                              @Param("unlockedEntityIds") Collection<Integer> unlockedEntityIds);

    @Query("""
            SELECT l FROM LevelEntity l WHERE l.number >
                        (SELECT le.number FROM LevelEntity le WHERE le.id = :levelId) ORDER BY l.number ASC
            """)
    List<LevelEntity> getNextLevel(@Param("levelId") int levelId, Pageable pageable);

}
