package com.btxtech.server.repository.director;

import com.btxtech.server.model.director.DirectorPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectorPlanRepository extends JpaRepository<DirectorPlanEntity, Integer> {
}
