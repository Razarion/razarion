package com.btxtech.server.repository;

import com.btxtech.server.model.ui.BrushConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrushConfigRepository extends JpaRepository<BrushConfigEntity, Integer> {
}
