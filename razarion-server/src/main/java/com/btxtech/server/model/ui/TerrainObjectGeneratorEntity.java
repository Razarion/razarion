package com.btxtech.server.model.ui;

import com.btxtech.server.service.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "TERRAIN_OBJECT_GENERATOR")
public class TerrainObjectGeneratorEntity extends BaseEntity {
    @Lob
    private String generatorJson;

    public String getGeneratorJson() {
        return generatorJson;
    }

    public void setGeneratorJson(String brushJson) {
        this.generatorJson = brushJson;
    }
}
