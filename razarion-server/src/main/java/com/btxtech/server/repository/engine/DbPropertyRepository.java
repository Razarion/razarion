package com.btxtech.server.repository.engine;

import com.btxtech.server.model.engine.DbPropertiesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbPropertyRepository extends JpaRepository<DbPropertiesEntity, String> {
}
