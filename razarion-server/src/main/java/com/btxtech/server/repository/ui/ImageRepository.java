package com.btxtech.server.repository.ui;

import com.btxtech.server.model.ui.ImageLibraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<ImageLibraryEntity, Integer> {
}
