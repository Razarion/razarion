package com.btxtech.server.repository.ui;

import com.btxtech.server.model.ui.GameUiContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameUiContextRepository extends JpaRepository<GameUiContextEntity, Integer> {
    @Query("SELECT g FROM GameUiContextEntity g JOIN g.minimalLevel l WHERE l.number <= :levelNumber ORDER BY l.number DESC")
    Optional<GameUiContextEntity> findTopByMinimalLevelNumber(@Param("levelNumber") int levelNumber);
}
