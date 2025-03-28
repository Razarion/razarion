package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.LevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelRepository extends JpaRepository<LevelEntity, Integer> {
    LevelEntity findTopByOrderByNumberAsc();

    @Query("SELECT l.number FROM LevelEntity l WHERE l.id = :levelId")
    Integer getLevelNumberByLevelId(@Param("levelId") int levelId);
}
