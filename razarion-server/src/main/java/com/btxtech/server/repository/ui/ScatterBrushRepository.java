package com.btxtech.server.repository.ui;

import com.btxtech.server.model.ui.ScatterBrushEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScatterBrushRepository extends JpaRepository<ScatterBrushEntity, Integer> {
}
