package com.btxtech.server.repository.ui;

import com.btxtech.server.model.ui.ImageLibraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<ImageLibraryEntity, Integer> {
    @Query(value = "SELECT data, type FROM IMAGE_LIBRARY WHERE id = :id", nativeQuery = true)
    List<Object[]> findImageRaw(@Param("id") int id);

}
