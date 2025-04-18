package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.AudioLibraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudioLibraryRepository extends JpaRepository<AudioLibraryEntity, Integer> {
}
